"""
Servicio de Dominio: MorosidadService
Implementa la Historia de Usuario HU-06 y el requisito funcional RF9.
Itera sobre los alquileres, filtra aquellos con pagos pendientes y fecha límite vencida,
y consolida la lista de negocios en mora para la administración.
"""

from datetime import date
from typing import List, Dict, Any, Optional
from collections import defaultdict
from domain.entities.alquiler import Alquiler, EstadoAlquiler
from domain.entities.negocio import Negocio
from domain.ports.alquiler_repository import IAlquilerRepository
from domain.ports.negocio_repository import INegocioRepository
from domain.ports.pago_repository import IPagoRepository
from domain.ports.usuario_repository import IUsuarioRepository


class MorosidadService:
    def __init__(
        self,
        alquiler_repo: IAlquilerRepository,
        negocio_repo: INegocioRepository,
        pago_repo: Optional[IPagoRepository] = None,
        usuario_repo: Optional[IUsuarioRepository] = None
    ):
        self._alquiler_repo = alquiler_repo
        self._negocio_repo = negocio_repo
        self._pago_repo = pago_repo
        self._usuario_repo = usuario_repo

    def obtener_negocios_en_mora(self, fecha_referencia: Optional[date] = None) -> List[Dict[str, Any]]:
        """
        HU-06: Itera sobre los alquileres del sistema, filtra aquellos con pagos pendientes
        cuya fecha límite ya venció, y devuelve la lista consolidada de negocios en mora.
        """
        fecha_corte = fecha_referencia or date.today()

        # 1. Obtener todos los alquileres registrados o pendientes
        alquileres = self._alquiler_repo.listar_todos()

        # 2. Iterar y filtrar los alquileres que presenten pagos pendientes vencidos
        alquileres_en_mora_por_negocio: Dict[int, List[Alquiler]] = defaultdict(list)

        for alq in alquileres:
            # Descartar alquileres cancelados
            if alq.estado == EstadoAlquiler.CANCELADO:
                continue

            # Hidratar pagos si el repo está configurado
            if self._pago_repo:
                pagos = self._pago_repo.listar_por_alquiler(alq.codigo)
                alq._pagos = list(pagos)

            # Actualizar estado a la fecha de corte
            alq.actualizarEstado(fecha_corte)

            # Regla de morosidad: Saldo > 0 y fecha de corte posterior a la fecha límite
            if alq.calcularSaldo() > 0 and fecha_corte > alq.fecha_limite:
                if alq.negocio_codigo is not None:
                    alquileres_en_mora_por_negocio[alq.negocio_codigo].append(alq)

        # 3. Consolidar el informe por cada negocio en mora
        resultado: List[Dict[str, Any]] = []

        for negocio_codigo, alqs_morosos in alquileres_en_mora_por_negocio.items():
            negocio = self._negocio_repo.buscar_por_codigo(negocio_codigo)
            if not negocio:
                continue

            # Obtener datos del propietario si el repositorio de usuarios está disponible
            propietario_info = None
            if self._usuario_repo and negocio.propietario_codigo:
                propietario = self._usuario_repo.buscar_por_codigo(negocio.propietario_codigo)
                if propietario:
                    propietario_info = {
                        "codigo": propietario.codigo,
                        "nombre_completo": propietario.nombre_completo,
                        "telefono": propietario.telefono,
                        "correo": propietario.correo
                    }

            total_mora = sum(a.calcularSaldo() for a in alqs_morosos)
            dias_max_retraso = max((fecha_corte - a.fecha_limite).days for a in alqs_morosos)

            resultado.append({
                "negocio_codigo": negocio.codigo,
                "negocio_nombre": negocio.nombre,
                "ubicacion": negocio.ubicacion,
                "estado_negocio": negocio.estado,
                "propietario": propietario_info,
                "total_deuda_en_mora": round(total_mora, 2),
                "cantidad_alquileres_en_mora": len(alqs_morosos),
                "dias_max_retraso": dias_max_retraso,
                "detalle_alquileres_mora": [
                    {
                        "alquiler_codigo": a.codigo,
                        "valor_total": a.valor,
                        "saldo_pendiente": a.calcularSaldo(),
                        "fecha_limite": a.fecha_limite.isoformat(),
                        "dias_retraso": (fecha_corte - a.fecha_limite).days
                    }
                    for a in alqs_morosos
                ]
            })

        # Ordenar por monto de morosidad descendente (los más críticos primero)
        resultado.sort(key=lambda item: item["total_deuda_en_mora"], reverse=True)
        return resultado
