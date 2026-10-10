"""
Entidad del Dominio: Alquiler
Representa la obligación de cobro periódica generada por el uso del local en la plaza cívica.
Encapsula la lógica de cálculo de saldo, gestión de estados y adición de pagos.
"""

from datetime import date
from typing import List, Optional
from domain.entities.pago import Pago
from domain.exceptions import (
    DatosInvalidosException,
    AlquilerYaPagadoException,
    PagoExcedeSaldoException,
)


class EstadoAlquiler:
    PENDIENTE = "PENDIENTE"
    PAGADO = "PAGADO"
    EN_MORA = "EN_MORA"
    CANCELADO = "CANCELADO"


class Alquiler:
    def __init__(
        self,
        codigo: int,
        fecha_inicio: date,
        fecha_limite: date,
        valor: float,
        estado: str = EstadoAlquiler.PENDIENTE,
        negocio_codigo: Optional[int] = None,
        pagos: Optional[List[Pago]] = None
    ):
        self._validar(codigo, fecha_inicio, fecha_limite, valor)
        self.codigo: int = codigo
        self.fecha_inicio: date = fecha_inicio
        self.fecha_limite: date = fecha_limite
        self.valor: float = float(valor)
        self.estado: str = estado.strip().upper()
        self.negocio_codigo: Optional[int] = negocio_codigo
        self._pagos: List[Pago] = list(pagos) if pagos else []

        # Asegurar estado consistente con el saldo actual al iniciar
        self.actualizarEstado()

    @staticmethod
    def _validar(codigo: int, fecha_inicio: date, fecha_limite: date, valor: float) -> None:
        if not isinstance(codigo, int) or codigo <= 0:
            raise DatosInvalidosException("El código del alquiler debe ser un entero positivo.")
        if not isinstance(fecha_inicio, date) or not isinstance(fecha_limite, date):
            raise DatosInvalidosException("Las fechas de inicio y límite deben ser objetos date válidos.")
        if fecha_inicio > fecha_limite:
            raise DatosInvalidosException("La fecha de inicio no puede ser posterior a la fecha límite.")
        if valor is None or valor <= 0:
            raise DatosInvalidosException("El valor del alquiler debe ser mayor a cero.")

    def calcularSaldo(self) -> float:
        """
        Método requerido por el diagrama de clases: calcularSaldo().
        Calcula el saldo pendiente restando los pagos acumulados del valor original.
        """
        total_pagado = sum(pago.monto for pago in self._pagos)
        saldo_restante = max(0.0, self.valor - total_pagado)
        return round(saldo_restante, 2)

    # Alias en snake_case
    calcular_saldo = calcularSaldo

    def actualizarEstado(self, fecha_referencia: Optional[date] = None) -> str:
        """
        Método requerido por el diagrama de clases: actualizarEstado().
        Actualiza el estado del alquiler según el saldo pendiente y la fecha límite.
        - Saldo == 0 -> PAGADO
        - Saldo > 0 y fecha_referencia > fecha_limite -> EN_MORA
        - Saldo > 0 y fecha_referencia <= fecha_limite -> PENDIENTE
        """
        if self.estado == EstadoAlquiler.CANCELADO:
            return self.estado

        saldo = self.calcularSaldo()
        if saldo == 0.0:
            self.estado = EstadoAlquiler.PAGADO
            return self.estado

        referencia = fecha_referencia or date.today()
        if referencia > self.fecha_limite:
            self.estado = EstadoAlquiler.EN_MORA
        else:
            self.estado = EstadoAlquiler.PENDIENTE

        return self.estado

    # Alias en snake_case
    actualizar_estado = actualizarEstado

    def agregar_pago(self, pago: Pago, fecha_referencia: Optional[date] = None) -> None:
        """
        Invariante de negocio: procesa y vincula un nuevo pago asegurando que
        no exceda la deuda y actualizando el estado inmediatamente en tiempo real.
        """
        if self.estado == EstadoAlquiler.PAGADO:
            raise AlquilerYaPagadoException(self.codigo)

        saldo_actual = self.calcularSaldo()
        # Manejo de tolerancia de redondeo en punto flotante
        if pago.monto > (saldo_actual + 0.001):
            raise PagoExcedeSaldoException(monto=pago.monto, saldo_pendiente=saldo_actual)

        pago.alquiler_codigo = self.codigo
        self._pagos.append(pago)
        self.actualizarEstado(fecha_referencia)

    def total_pagado(self) -> float:
        return round(sum(p.monto for p in self._pagos), 2)

    def esta_en_mora(self, fecha_referencia: Optional[date] = None) -> bool:
        """Determina si el alquiler se encuentra actualmente en mora."""
        self.actualizarEstado(fecha_referencia)
        return self.estado == EstadoAlquiler.EN_MORA

    def obtener_pagos(self) -> List[Pago]:
        """Devuelve una copia defensiva de los pagos asociados a este alquiler."""
        return list(self._pagos)

    def __repr__(self) -> str:
        return (
            f"<Alquiler(codigo={self.codigo}, valor={self.valor}, saldo={self.calcularSaldo()}, "
            f"estado='{self.estado}', limite={self.fecha_limite})>"
        )
