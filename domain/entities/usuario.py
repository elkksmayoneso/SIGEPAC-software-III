"""
Entidad del Dominio: Usuario
Representa a los usuarios del sistema (Administradora o Propietario).
Conforme a los requerimientos:
- No incluye lógica de autenticación ni validación de contraseñas (RF10/HU-01 ya implementados).
- Encapsula estado y validaciones para evitar un modelo anémico.
"""

from typing import Optional
from domain.exceptions import DatosInvalidosException


class Usuario:
    def __init__(
        self,
        codigo: int,
        nombre: str,
        apellido: str,
        correo: str,
        contrasena: str,
        tipo: str,
        telefono: str
    ):
        self._validar_datos_iniciales(codigo, nombre, apellido, correo, tipo, telefono)
        self.codigo: int = codigo
        self.nombre: str = nombre.strip()
        self.apellido: str = apellido.strip()
        self.correo: str = correo.strip().lower()
        self.contrasena: str = contrasena  # Preservada según diagrama de clases, sin lógica de auth
        self.tipo: str = tipo.strip().upper()  # Ej: 'PROPIETARIO', 'ADMINISTRADORA'
        self.telefono: str = telefono.strip()

    @staticmethod
    def _validar_datos_iniciales(
        codigo: int,
        nombre: str,
        apellido: str,
        correo: str,
        tipo: str,
        telefono: str
    ) -> None:
        if not isinstance(codigo, int) or codigo <= 0:
            raise DatosInvalidosException("El código de usuario debe ser un entero positivo.")
        if not nombre or not nombre.strip():
            raise DatosInvalidosException("El nombre del usuario no puede estar vacío.")
        if not apellido or not apellido.strip():
            raise DatosInvalidosException("El apellido del usuario no puede estar vacío.")
        if not correo or "@" not in correo:
            raise DatosInvalidosException("El correo del usuario no tiene un formato válido.")
        if not tipo or tipo.strip().upper() not in {"PROPIETARIO", "ADMINISTRADORA"}:
            raise DatosInvalidosException("El tipo de usuario debe ser 'PROPIETARIO' o 'ADMINISTRADORA'.")
        if not telefono or not telefono.strip():
            raise DatosInvalidosException("El teléfono no puede estar vacío.")

    def actualizar_datos(
        self,
        nombre: Optional[str] = None,
        apellido: Optional[str] = None,
        correo: Optional[str] = None,
        telefono: Optional[str] = None
    ) -> None:
        """Permite la edición controlada de los datos del usuario."""
        if nombre is not None:
            if not nombre.strip():
                raise DatosInvalidosException("El nombre no puede quedar vacío.")
            self.nombre = nombre.strip()

        if apellido is not None:
            if not apellido.strip():
                raise DatosInvalidosException("El apellido no puede quedar vacío.")
            self.apellido = apellido.strip()

        if correo is not None:
            if not correo.strip() or "@" not in correo:
                raise DatosInvalidosException("El correo proporcionado no es válido.")
            self.correo = correo.strip().lower()

        if telefono is not None:
            if not telefono.strip():
                raise DatosInvalidosException("El teléfono no puede quedar vacío.")
            self.telefono = telefono.strip()

    def es_propietario(self) -> bool:
        """Regla de negocio: verifica si el rol del usuario corresponde a un propietario."""
        return self.tipo == "PROPIETARIO"

    def es_administradora(self) -> bool:
        """Regla de negocio: verifica si el rol corresponde a la administradora."""
        return self.tipo == "ADMINISTRADORA"

    @property
    def nombre_completo(self) -> str:
        return f"{self.nombre} {self.apellido}"

    def __repr__(self) -> str:
        return f"<Usuario(codigo={self.codigo}, nombre='{self.nombre_completo}', tipo='{self.tipo}')>"
