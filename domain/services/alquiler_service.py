"""
Servicio de Dominio: AlquilerService
Implementa los Requisitos Funcionales RF5, RF7, RF9 y la Historia de Usuario HU-04.
Permite la gestión concurrente y consistente de múltiples periodos de alquiler por negocio (RNF5).
"""

from datetime import date
from typing import List, Optional
from domain.entities.alquiler import Alquiler, EstadoAlquiler
from domain.ports.alquiler_repository import IAlquilerRepository
from domain.ports.negocio_repository import INegocioRepository
from domain.ports.pago_repository import IPagoRepository
from domain.exceptions import (
    AlquilerNoEncontradoException,
    NegocioNoEncontradoException,
    ReglaNegocioException,
)


class AlquilerService:
    def __init__(
        self,
        alquiler_repo: IAlquilerRepository,
        negocio_repo: INegocioRepository,
        pago_repo: Optional[IPagoRepository] = None
    ):
        self._alquiler_repo = alquiler_repo
        self._negocio_repo = negocio_repo
        self._pago_repo = pago_repo

    def registrar_alquiler(
        self,
        codigo: int,
        negocio_codigo: int,
        fecha_inicio: date,
        fecha_limite: date,
        valor: float
    ) -> Alquiler:
        """
        RF5 y HU-04: Registra una cuota o contrato de alquiler y asocia directamente
        el valor de la deuda al negocio indicado.
        """
        if self._alquiler_repo.buscar_por_codigo(codigo):
            raise ReglaNegocioException(f"Ya existe un alquiler con el código {codigo}.")

        # HU-04: Verificar existencia del negocio al que se asocia la deuda
        negocio = self._negocio_repo.buscar_por_codigo(negocio_codigo)
        if not negocio:
            raise NegocioNoEncontradoException(negocio_codigo)

        alquiler = Alquiler(
            codigo=codigo,
            fecha_inicio=fecha_inicio,
            fecha_limite=fecha_limite,
            valor=valor,
            estado=EstadoAlquiler.PENDIENTE,
            negocio_codigo=negocio_codigo
        )

        alquiler_guardado = self._alquiler_repo.guardar(alquiler)
        negocio.asociar_alquiler(alquiler_guardado)
        return alquiler_guardado

    def consultar_alquiler(self, codigo: int) -> Alquiler:
        """Consulta un alquiler e hidrata sus pagos si el repositorio de pagos está disponible."""
        alquiler = self._alquiler_repo.buscar_por_codigo(codigo)
        if not alquiler:
            raise AlquilerNoEncontradoException(codigo)

        if self._pago_repo:
            pagos = self._pago_repo.listar_por_alquiler(codigo)
            for p in pagos:
                if not any(existente.codigo == p.codigo for existente in alquiler.obtener_pagos()):
                    alquiler.agregar_pago(p)

        return alquiler

    def consultar_alquileres_por_negocio(self, negocio_codigo: int) -> List[Alquiler]:
        """RF7: Consultar todos los alquileres asociados a un negocio."""
        negocio = self._negocio_repo.buscar_por_codigo(negocio_codigo)
        if not negocio:
            raise NegocioNoEncontradoException(negocio_codigo)

        alquileres = self._alquiler_repo.listar_por_negocio(negocio_codigo)
        if self._pago_repo:
            for alq in alquileres:
                pagos = self._pago_repo.listar_por_alquiler(alq.codigo)
                for p in pagos:
                    if not any(existente.codigo == p.codigo for existente in alq.obtener_pagos()):
                        alq.agregar_pago(p)

        return alquileres

    def obtener_alquileres_pendientes(self, fecha_referencia: Optional[date] = None) -> List[Alquiler]:
        """
        RF9: Identificar alquileres con pagos pendientes (PENDIENTE o EN_MORA).
        Actualiza el estado dinámicamente con respecto a la fecha dada.
        """
        todos = self._alquiler_repo.listar_todos()
        fecha_corte = fecha_referencia or date.today()
        pendientes = []

        for alq in todos:
            if self._pago_repo:
                pagos = self._pago_repo.listar_por_alquiler(alq.codigo)
                for p in pagos:
                    if not any(existente.codigo == p.codigo for existente in alq.obtener_pagos()):
                        alq.agregar_pago(p)

            alq.actualizarEstado(fecha_corte)
            if alq.calcularSaldo() > 0:
                pendientes.append(alq)

        return pendientes

    def eliminar_alquiler(self, codigo: int) -> bool:
        """
        Elimina un registro de alquiler solo si no posee pagos registrados
        para preservar la integridad contable.
        """
        alquiler = self._alquiler_repo.buscar_por_codigo(codigo)
        if not alquiler:
            raise AlquilerNoEncontradoException(codigo)

        if self._pago_repo:
            pagos = self._pago_repo.listar_por_alquiler(codigo)
            if pagos:
                raise ReglaNegocioException(
                    f"No se puede eliminar el alquiler {codigo} porque posee {len(pagos)} pago(s) registrado(s)."
                )

        return self._alquiler_repo.eliminar(codigo)
