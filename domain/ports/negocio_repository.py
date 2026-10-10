"""
Puerto Secundario (Driven Port): INegocioRepository
Define el contrato para la persistencia y recuperación de entidades Negocio.
"""

from abc import ABC, abstractmethod
from typing import List, Optional
from domain.entities.negocio import Negocio


class INegocioRepository(ABC):

    @abstractmethod
    def guardar(self, negocio: Negocio) -> Negocio:
        """Persiste un nuevo negocio en el sistema."""
        pass

    @abstractmethod
    def buscar_por_codigo(self, codigo: int) -> Optional[Negocio]:
        """Recupera un negocio por su código único."""
        pass

    @abstractmethod
    def buscar_por_ubicacion(self, ubicacion: str) -> Optional[Negocio]:
        """Busca si existe un negocio asignado a una ubicación o local comercial específico."""
        pass

    @abstractmethod
    def actualizar(self, negocio: Negocio) -> Negocio:
        """Actualiza los datos de un negocio existente."""
        pass

    @abstractmethod
    def eliminar(self, codigo: int) -> bool:
        """Elimina un negocio del sistema."""
        pass

    @abstractmethod
    def listar_todos(self) -> List[Negocio]:
        """Retorna todos los negocios registrados."""
        pass

    @abstractmethod
    def listar_por_propietario(self, propietario_codigo: int) -> List[Negocio]:
        """Retorna todos los negocios pertenecientes a un propietario."""
        pass
