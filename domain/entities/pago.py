"""
Entidad del Dominio: Pago
Representa los pagos o abonos realizados a un alquiler de la administración.
Cumple con el diagrama de clases y los requerimientos de no ser un modelo anémico.
"""

from datetime import date
from typing import Optional, Dict, Any
from domain.exceptions import DatosInvalidosException, MontoPagoInvalidoException


class Pago:
    def __init__(
        self,
        codigo: int,
        monto: float,
        fecha_pago: date,
        tipo_pago: str,
        alquiler_codigo: Optional[int] = None,
        codigo_metodo: Optional[int] = None
    ):
        self._validar_datos(codigo, monto, fecha_pago, tipo_pago)
        self.codigo: int = codigo
        self.monto: float = float(monto)
        self.fecha_pago: date = fecha_pago
        self.tipo_pago: str = tipo_pago.strip()
        self.alquiler_codigo: Optional[int] = alquiler_codigo
        self.codigo_metodo: Optional[int] = codigo_metodo

    @staticmethod
    def _validar_datos(codigo: int, monto: float, fecha_pago: date, tipo_pago: str) -> None:
        if not isinstance(codigo, int) or codigo <= 0:
            raise DatosInvalidosException("El código del pago debe ser un entero positivo.")
        if monto is None or monto <= 0:
            raise MontoPagoInvalidoException(monto)
        if not isinstance(fecha_pago, date):
            raise DatosInvalidosException("La fecha de pago debe ser una instancia válida de date.")
        if not tipo_pago or not tipo_pago.strip():
            raise DatosInvalidosException("El tipo de pago no puede estar vacío.")

    def registrarPago(
        self,
        monto: float,
        fecha_pago: date,
        tipo_pago: str,
        alquiler_codigo: Optional[int] = None
    ) -> None:
        """
        Método requerido por el diagrama de clases: registrarPago().
        Encapsula la lógica de verificación y aplicación del pago en la entidad.
        """
        self._validar_datos(self.codigo, monto, fecha_pago, tipo_pago)
        self.monto = float(monto)
        self.fecha_pago = fecha_pago
        self.tipo_pago = tipo_pago.strip()
        if alquiler_codigo is not None:
            self.alquiler_codigo = alquiler_codigo

    # Alias en snake_case
    registrar_pago = registrarPago

    def to_dict(self) -> Dict[str, Any]:
        """Representación estructurada del pago para consultas e historiales."""
        return {
            "codigo": self.codigo,
            "monto": self.monto,
            "fecha_pago": self.fecha_pago.isoformat(),
            "tipo_pago": self.tipo_pago,
            "alquiler_codigo": self.alquiler_codigo,
            "codigo_metodo": self.codigo_metodo
        }

    def __repr__(self) -> str:
        return f"<Pago(codigo={self.codigo}, monto={self.monto}, fecha={self.fecha_pago}, tipo='{self.tipo_pago}')>"
