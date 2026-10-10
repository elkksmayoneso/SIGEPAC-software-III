"""
Servicio de Dominio: PagoService
Implementa los Requisitos Funcionales RF6, RF8 y las Historias de Usuario HU-05 y HU-08.
Garantiza que al registrar un pago se verifique el negocio, se descuente el saldo en tiempo real,
y se mantenga un historial cronológico inmutable y ordenado.
"""

from datetime import date
from typing import List, Optional, Dict, Any
from domain.entities.pago import Pago
from domain.entities.alquiler import Alquiler
from domain.ports.pago_repository import IPagoRepository
from domain.ports.alquiler_repository import IAlquilerRepository
from domain.ports.negocio_repository import INegocioRepository
from domain.ports.metodo_pago_repository import IMetodoPagoRepository
from domain.exceptions import (
    NegocioNoEncontradoException,
    AlquilerNoEncontradoException,
    MetodoPagoNoEncontradoException,
    ReglaNegocioException,
)


class PagoService:
    def __init__(
        self,
        pago_repo: IPagoRepository,
        alquiler_repo: IAlquilerRepository,
        negocio_repo: INegocioRepository,
        metodo_repo: Optional[IMetodoPagoRepository] = None
    ):
        self._pago_repo = pago_repo
        self._alquiler_repo = alquiler_repo
        self._negocio_repo = negocio_repo
        self._metodo_repo = metodo_repo

    def registrar_pago(
        self,
        codigo: int,
        negocio_codigo: int,
        alquiler_codigo: int,
        monto: float,
        fecha_pago: date,
        tipo_pago: str,
        codigo_metodo: Optional[int] = None
    ) -> Pago:
        """
        RF6 y HU-05: Lógica de Registro de Pagos.
        1. Verifica que el negocio exista en el dominio.
        2. Verifica que el alquiler exista y pertenezca a dicho negocio.
        3. Verifica el método de pago si aplica.
        4. Descuenta el saldo y actualiza el estado del alquiler/deuda en tiempo real.
        5. Persiste el pago y el estado actualizado.
        """
        # Validar unicidad del pago
        if self._pago_repo.buscar_por_codigo(codigo):
            raise ReglaNegocioException(f"Ya existe un pago registrado con el código {codigo}.")

        # HU-05 Regla 1: Verificar que el negocio exista
        negocio = self._negocio_repo.buscar_por_codigo(negocio_codigo)
        if not negocio:
            raise NegocioNoEncontradoException(negocio_codigo)

        # Verificar que el alquiler exista
        alquiler = self._alquiler_repo.buscar_por_codigo(alquiler_codigo)
        if not alquiler:
            raise AlquilerNoEncontradoException(alquiler_codigo)

        # Verificar que el alquiler corresponda efectivamente a ese negocio
        if alquiler.negocio_codigo != negocio_codigo:
            raise ReglaNegocioException(
                f"El alquiler {alquiler_codigo} no pertenece al negocio {negocio_codigo}."
            )

        # Validar método de pago si se suministra
        if codigo_metodo is not None and self._metodo_repo is not None:
            metodo = self._metodo_repo.buscar_por_codigo(codigo_metodo)
            if not metodo:
                raise MetodoPagoNoEncontradoException(codigo_metodo)
            tipo_pago = metodo.nombre_metodo

        # Hidratar pagos existentes para conocer el saldo exacto en tiempo real
        pagos_existentes = self._pago_repo.listar_por_alquiler(alquiler_codigo)
        alquiler._pagos = list(pagos_existentes)

        # HU-05 Regla 2: Crear el pago y procesarlo en la entidad Alquiler.
        # Esto valida que no sobrepase el saldo pendiente y actualiza el estado (PENDIENTE -> PAGADO / EN_MORA)
        pago = Pago(
            codigo=codigo,
            monto=monto,
            fecha_pago=fecha_pago,
            tipo_pago=tipo_pago,
            alquiler_codigo=alquiler_codigo,
            codigo_metodo=codigo_metodo
        )
        pago.registrarPago(monto=monto, fecha_pago=fecha_pago, tipo_pago=tipo_pago, alquiler_codigo=alquiler_codigo)

        # Aplicar el pago al agregado Alquiler
        alquiler.agregar_pago(pago, fecha_referencia=fecha_pago)

        # Persistir el nuevo pago y el nuevo estado del alquiler
        pago_guardado = self._pago_repo.guardar(pago)
        self._alquiler_repo.actualizar(alquiler)

        return pago_guardado

    def obtener_historial_cronologico(self, negocio_codigo: int, ascendente: bool = True) -> List[Dict[str, Any]]:
        """
        HU-08 y RF8: Recupera la lista cronológica con fechas y montos de pagos
        realizados por los alquileres de un negocio.
        """
        negocio = self._negocio_repo.buscar_por_codigo(negocio_codigo)
        if not negocio:
            raise NegocioNoEncontradoException(negocio_codigo)

        # Si el repositorio soporta la consulta optimizada, la usamos; de lo contrario agregamos
        pagos = self._pago_repo.listar_por_negocio_cronologico(negocio_codigo)
        if not pagos:
            # Plan B: buscar a través de los alquileres del negocio
            alquileres = self._alquiler_repo.listar_por_negocio(negocio_codigo)
            todos_pagos: List[Pago] = []
            for alq in alquileres:
                todos_pagos.extend(self._pago_repo.listar_por_alquiler(alq.codigo))
            pagos = todos_pagos

        # Ordenar cronológicamente por fecha_pago y luego por código
        pagos_ordenados = sorted(
            pagos,
            key=lambda p: (p.fecha_pago, p.codigo),
            reverse=not ascendente
        )

        return [
            {
                "codigo_pago": p.codigo,
                "fecha_pago": p.fecha_pago.isoformat(),
                "monto": p.monto,
                "tipo_pago": p.tipo_pago,
                "alquiler_codigo": p.alquiler_codigo,
                "negocio_codigo": negocio_codigo
            }
            for p in pagos_ordenados
        ]

    def listar_pagos_por_alquiler(self, alquiler_codigo: int) -> List[Pago]:
        """RF8: Visualizar los pagos asociados a un alquiler específico."""
        alquiler = self._alquiler_repo.buscar_por_codigo(alquiler_codigo)
        if not alquiler:
            raise AlquilerNoEncontradoException(alquiler_codigo)
        return self._pago_repo.listar_por_alquiler(alquiler_codigo)
