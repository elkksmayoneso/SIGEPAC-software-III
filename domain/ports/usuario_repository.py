"""
Puerto Secundario (Driven Port): IUsuarioRepository
Define el contrato para la persistencia y recuperación de entidades Usuario.
Totalmente desacoplado de bases de datos o librerías externas (RNF7).
"""

from abc import ABC, abstractmethod
from typing import List, Optional
from domain.entities.usuario import Usuario


class IUsuarioRepository(ABC):

    @abstractmethod
    def guardar(self, usuario: Usuario) -> Usuario:
        """Persiste un nuevo usuario en el almacén de datos."""
        pass

    @abstractmethod
    def buscar_por_codigo(self, codigo: int) -> Optional[Usuario]:
        """Recupera un usuario por su código identificador único."""
        pass

    @abstractmethod
    def buscar_por_correo(self, correo: str) -> Optional[Usuario]:
        """Busca un usuario por su correo electrónico."""
        pass

    @abstractmethod
    def actualizar(self, usuario: Usuario) -> Usuario:
        """Actualiza la información persistida de un usuario existente."""
        pass

    @abstractmethod
    def eliminar(self, codigo: int) -> bool:
        """Elimina el registro de un usuario por su código."""
        pass

    @abstractmethod
    def listar_todos(self) -> List[Usuario]:
        """Obtiene la lista completa de usuarios registrados."""
        pass
