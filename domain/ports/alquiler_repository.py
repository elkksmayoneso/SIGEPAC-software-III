"""
Puerto Secundario (Driven Port): IAlquilerRepository
Define el contrato para la persistencia y recuperación de entidades Alquiler.
"""

from abc import ABC, abstractmethod
from typing import List, Optional
from domain.entities.alquiler import Alquiler


class IAlquilerRepository(ABC):

    @abstractmethod
    def guardar(self, alquiler: Alquiler) -> Alquiler:
        """Persiste un nuevo contrato/cuota de alquiler."""
        pass

    @abstractmethod
    def buscar_por_codigo(self, codigo: int) -> Optional[Alquiler]:
        """Busca un alquiler por su identificador único."""
        pass

    @abstractmethod
    def actualizar(self, alquiler: Alquiler) -> Alquiler:
        """Actualiza el estado y datos de un alquiler."""
        pass

    @abstractmethod
    def eliminar(self, codigo: int) -> bool:
        """Elimina un registro de alquiler."""
        pass

    @abstractmethod
    def listar_todos(self) -> List[Alquiler]:
        """Retorna todos los alquileres existentes en la plataforma."""
        pass

    @abstractmethod
    def listar_por_negocio(self, negocio_codigo: int) -> List[Alquiler]:
        """Obtiene la lista de alquileres asociados a un determinado negocio."""
        pass

    @abstractmethod
    def listar_pendientes(self) -> List[Alquiler]:
        """RF9: Identifica y retorna todos los alquileres con pagos pendientes o en mora."""
        pass
