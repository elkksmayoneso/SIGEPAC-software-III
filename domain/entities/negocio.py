"""
Entidad del Dominio: Negocio
Representa el local comercial en la plaza cívica que contrae obligaciones de alquiler y realiza pagos.
Encapsula la colección de alquileres, el enlace al propietario y el cálculo de la deuda total.
"""

from datetime import date
from typing import List, Optional
from domain.entities.alquiler import Alquiler, EstadoAlquiler
from domain.entities.usuario import Usuario
from domain.exceptions import (
    DatosInvalidosException,
    UsuarioNoEsPropietarioException,
)


class EstadoNegocio:
    ACTIVO = "ACTIVO"
    INACTIVO = "INACTIVO"


class Negocio:
    def __init__(
        self,
        codigo: int,
        nombre: str,
        ubicacion: str,
        estado: str = EstadoNegocio.ACTIVO,
        fecha_registro: Optional[date] = None,
        propietario_codigo: Optional[int] = None,
        alquileres: Optional[List[Alquiler]] = None
    ):
        self._validar(codigo, nombre, ubicacion)
        self.codigo: int = codigo
        self.nombre: str = nombre.strip()
        self.ubicacion: str = ubicacion.strip()
        self.estado: str = estado.strip().upper()
        self.fecha_registro: date = fecha_registro or date.today()
        self.propietario_codigo: Optional[int] = propietario_codigo
        self._alquileres: List[Alquiler] = list(alquileres) if alquileres else []

    @staticmethod
    def _validar(codigo: int, nombre: str, ubicacion: str) -> None:
        if not isinstance(codigo, int) or codigo <= 0:
            raise DatosInvalidosException("El código del negocio debe ser un entero positivo.")
        if not nombre or not nombre.strip():
            raise DatosInvalidosException("El nombre del negocio no puede estar vacío.")
        if not ubicacion or not ubicacion.strip():
            raise DatosInvalidosException("La ubicación (local comercial) no puede estar vacía.")

    def obtenerAlquileres(self) -> List[Alquiler]:
        """
        Método requerido por el diagrama de clases: obtenerAlquileres().
        Devuelve una copia de la lista de alquileres asociados al negocio.
        """
        return list(self._alquileres)

    # Alias en snake_case
    obtener_alquileres = obtenerAlquileres

    def asociar_propietario(self, propietario: Usuario) -> None:
        """
        HU-03: Asocia el negocio a un propietario existente, verificando la regla de negocio
        de que el usuario tenga rol de 'PROPIETARIO'.
        """
        if not propietario.es_propietario():
            raise UsuarioNoEsPropietarioException(codigo=propietario.codigo, tipo=propietario.tipo)
        self.propietario_codigo = propietario.codigo

    def asociar_alquiler(self, alquiler: Alquiler) -> None:
        """
        HU-04: Asocia un alquiler directamente a este negocio.
        """
        alquiler.negocio_codigo = self.codigo
        # Evitar duplicados por código
        if not any(a.codigo == alquiler.codigo for a in self._alquileres):
            self._alquileres.append(alquiler)

    def calcular_deuda_total(self) -> float:
        """
        Calcula la deuda acumulada sumando los saldos pendientes de todos los alquileres activos.
        """
        total = sum(
            alq.calcularSaldo()
            for alq in self._alquileres
            if alq.estado != EstadoAlquiler.CANCELADO
        )
        return round(total, 2)

    def esta_en_mora(self, fecha_referencia: Optional[date] = None) -> bool:
        """
        HU-06: Determina si el negocio tiene al menos un alquiler vencido no pagado.
        """
        return any(alq.esta_en_mora(fecha_referencia) for alq in self._alquileres)

    def esta_al_dia(self, fecha_referencia: Optional[date] = None) -> bool:
        """
        HU-07: Determina si el negocio se encuentra al día (sin deudas en mora).
        """
        return not self.esta_en_mora(fecha_referencia)

    def actualizar_datos(
        self,
        nombre: Optional[str] = None,
        ubicacion: Optional[str] = None,
        estado: Optional[str] = None
    ) -> None:
        """Permite editar información del negocio manteniendo las invariantes."""
        if nombre is not None:
            if not nombre.strip():
                raise DatosInvalidosException("El nombre no puede estar vacío.")
            self.nombre = nombre.strip()

        if ubicacion is not None:
            if not ubicacion.strip():
                raise DatosInvalidosException("La ubicación no puede estar vacía.")
            self.ubicacion = ubicacion.strip()

        if estado is not None:
            if estado.strip().upper() not in {EstadoNegocio.ACTIVO, EstadoNegocio.INACTIVO}:
                raise DatosInvalidosException("El estado debe ser 'ACTIVO' o 'INACTIVO'.")
            self.estado = estado.strip().upper()

    def __repr__(self) -> str:
        return (
            f"<Negocio(codigo={self.codigo}, nombre='{self.nombre}', ubicacion='{self.ubicacion}', "
            f"propietario_codigo={self.propietario_codigo}, deuda_total={self.calcular_deuda_total()})>"
        )
