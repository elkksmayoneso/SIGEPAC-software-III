"""
Servicio de Dominio: NegocioService
Implementa los Requisitos Funcionales RF1 a RF4 y las Historias de Usuario HU-03 y HU-07.
Coordina la asociación a locales y propietarios, la edición de locales y la consulta del estado de deuda.
"""

from datetime import date
from typing import List, Optional, Dict, Any
from domain.entities.negocio import Negocio, EstadoNegocio
from domain.ports.negocio_repository import INegocioRepository
from domain.ports.usuario_repository import IUsuarioRepository
from domain.ports.alquiler_repository import IAlquilerRepository
from domain.exceptions import (
    NegocioNoEncontradoException,
    UsuarioNoEncontradoException,
    ReglaNegocioException,
)


class NegocioService:
    def __init__(
        self,
        negocio_repo: INegocioRepository,
        usuario_repo: IUsuarioRepository,
        alquiler_repo: Optional[IAlquilerRepository] = None
    ):
        self._negocio_repo = negocio_repo
        self._usuario_repo = usuario_repo
        self._alquiler_repo = alquiler_repo

    def registrar_negocio(
        self,
        codigo: int,
        nombre: str,
        ubicacion: str,
        propietario_codigo: int,
        estado: str = EstadoNegocio.ACTIVO,
        fecha_registro: Optional[date] = None
    ) -> Negocio:
        """
        RF1 y HU-03: Registra un negocio asociándolo a un local físico (ubicación)
        y a un propietario existente con rol de 'PROPIETARIO'.
        """
        # Validar unicidad del código de negocio
        if self._negocio_repo.buscar_por_codigo(codigo):
            raise ReglaNegocioException(f"Ya existe un negocio con el código {codigo}.")

        # Validar que el propietario exista y sea efectivamente un PROPIETARIO
        propietario = self._usuario_repo.buscar_por_codigo(propietario_codigo)
        if not propietario:
            raise UsuarioNoEncontradoException(propietario_codigo)

        # Validar que no haya otro negocio ACTIVO en el mismo local comercial (ubicación)
        existente_ubicacion = self._negocio_repo.buscar_por_ubicacion(ubicacion)
        if existente_ubicacion and existente_ubicacion.estado == EstadoNegocio.ACTIVO:
            raise ReglaNegocioException(
                f"El local '{ubicacion}' ya está asignado al negocio activo '{existente_ubicacion.nombre}'."
            )

        negocio = Negocio(
            codigo=codigo,
            nombre=nombre,
            ubicacion=ubicacion,
            estado=estado,
            fecha_registro=fecha_registro or date.today()
        )
        negocio.asociar_propietario(propietario)

        return self._negocio_repo.guardar(negocio)

    def editar_negocio(
        self,
        codigo: int,
        nombre: Optional[str] = None,
        ubicacion: Optional[str] = None,
        estado: Optional[str] = None
    ) -> Negocio:
        """RF2: Editar la información del negocio (nombre, local comercial o estado)."""
        negocio = self._negocio_repo.buscar_por_codigo(codigo)
        if not negocio:
            raise NegocioNoEncontradoException(codigo)

        if ubicacion and ubicacion != negocio.ubicacion:
            otro_en_local = self._negocio_repo.buscar_por_ubicacion(ubicacion)
            if otro_en_local and otro_en_local.codigo != codigo and otro_en_local.estado == EstadoNegocio.ACTIVO:
                raise ReglaNegocioException(f"El local '{ubicacion}' ya está ocupado por otro negocio activo.")

        negocio.actualizar_datos(nombre=nombre, ubicacion=ubicacion, estado=estado)
        return self._negocio_repo.actualizar(negocio)

    def eliminar_negocio(self, codigo: int) -> bool:
        """
        RF3/RF4: Elimina un registro de negocio.
        Regla de negocio: No se puede eliminar si tiene alquileres con saldos pendientes.
        """
        negocio = self._negocio_repo.buscar_por_codigo(codigo)
        if not negocio:
            raise NegocioNoEncontradoException(codigo)

        if self._alquiler_repo:
            alquileres = self._alquiler_repo.listar_por_negocio(codigo)
            deuda_pendiente = sum(a.calcularSaldo() for a in alquileres)
            if deuda_pendiente > 0:
                raise ReglaNegocioException(
                    f"No se puede eliminar el negocio '{negocio.nombre}' porque posee deudas pendientes por {deuda_pendiente}."
                )

        return self._negocio_repo.eliminar(codigo)

    def consultar_negocio(self, codigo: int) -> Negocio:
        """Consulta un negocio e hidrata sus alquileres asociados si está disponible el repositorio."""
        negocio = self._negocio_repo.buscar_por_codigo(codigo)
        if not negocio:
            raise NegocioNoEncontradoException(codigo)

        if self._alquiler_repo:
            alquileres = self._alquiler_repo.listar_por_negocio(codigo)
            for alq in alquileres:
                negocio.asociar_alquiler(alq)

        return negocio

    def asociar_propietario_a_negocio(self, negocio_codigo: int, propietario_codigo: int) -> Negocio:
        """HU-03: Permite reasignar o vincular un propietario existente a un negocio."""
        negocio = self._negocio_repo.buscar_por_codigo(negocio_codigo)
        if not negocio:
            raise NegocioNoEncontradoException(negocio_codigo)

        propietario = self._usuario_repo.buscar_por_codigo(propietario_codigo)
        if not propietario:
            raise UsuarioNoEncontradoException(propietario_codigo)

        negocio.asociar_propietario(propietario)
        return self._negocio_repo.actualizar(negocio)

    def consultar_estado_deuda(
        self,
        negocio_codigo: int,
        fecha_referencia: Optional[date] = None
    ) -> Dict[str, Any]:
        """
        HU-07: Lógica para retornar el estado actual de la deuda (monto pendiente o al día).
        Calcula si el negocio tiene saldo pendiente y si está al día o en mora.
        """
        negocio = self.consultar_negocio(negocio_codigo)
        fecha_corte = fecha_referencia or date.today()

        alquileres = negocio.obtenerAlquileres()
        total_deuda = 0.0
        total_mora = 0.0
        detalle_alquileres = []

        for alq in alquileres:
            saldo = alq.calcularSaldo()
            alq.actualizarEstado(fecha_corte)
            total_deuda += saldo
            if alq.esta_en_mora(fecha_corte):
                total_mora += saldo

            detalle_alquileres.append({
                "alquiler_codigo": alq.codigo,
                "valor_original": alq.valor,
                "saldo_pendiente": saldo,
                "total_pagado": alq.total_pagado(),
                "fecha_inicio": alq.fecha_inicio.isoformat(),
                "fecha_limite": alq.fecha_limite.isoformat(),
                "estado": alq.estado
            })

        esta_al_dia = (total_mora == 0.0 and total_deuda == 0.0)

        return {
            "negocio_codigo": negocio.codigo,
            "negocio_nombre": negocio.nombre,
            "ubicacion": negocio.ubicacion,
            "propietario_codigo": negocio.propietario_codigo,
            "fecha_corte": fecha_corte.isoformat(),
            "deuda_total_pendiente": round(total_deuda, 2),
            "deuda_vencida_mora": round(total_mora, 2),
            "estado_cuenta": "AL_DIA" if esta_al_dia else ("EN_MORA" if total_mora > 0 else "CON_SALDO_PENDIENTE"),
            "alquileres": detalle_alquileres
        }

    def listar_negocios(self) -> List[Negocio]:
        """Lista todos los negocios registrados."""
        return self._negocio_repo.listar_todos()

    def listar_negocios_por_propietario(self, propietario_codigo: int) -> List[Negocio]:
        """Consulta los locales o negocios pertenecientes a un propietario."""
        return self._negocio_repo.listar_por_propietario(propietario_codigo)
