"""
Entidad del Dominio: Metodo_pago
Representa las formas de pago aceptadas en la plaza cívica (Efectivo, Transferencia, etc.).
"""

from typing import Dict, Any
from domain.exceptions import DatosInvalidosException


class MetodoPago:
    def __init__(self, codigo_metodo: int, nombre_metodo: str):
        self._validar(codigo_metodo, nombre_metodo)
        self.codigo_metodo: int = codigo_metodo
        self.nombre_metodo: str = nombre_metodo.strip().upper()

    @staticmethod
    def _validar(codigo_metodo: int, nombre_metodo: str) -> None:
        if not isinstance(codigo_metodo, int) or codigo_metodo <= 0:
            raise DatosInvalidosException("El código del método de pago debe ser un entero positivo.")
        if not nombre_metodo or not nombre_metodo.strip():
            raise DatosInvalidosException("El nombre del método de pago no puede estar vacío.")

    def obtenerMetodo(self) -> Dict[str, Any]:
        """
        Método requerido por el diagrama de clases: obtenerMetodo().
        Retorna los detalles estructurados del método de pago.
        """
        return {
            "codigo_metodo": self.codigo_metodo,
            "nombre_metodo": self.nombre_metodo
        }

    # Alias en snake_case para convenciones de Python
    obtener_metodo = obtenerMetodo

    def __repr__(self) -> str:
        return f"<MetodoPago(codigo={self.codigo_metodo}, nombre='{self.nombre_metodo}')>"
