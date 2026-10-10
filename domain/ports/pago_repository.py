"""
Puerto Secundario (Driven Port): IPagoRepository
Define el contrato para la persistencia y recuperación de los pagos registrados.
"""

from abc import ABC, abstractmethod
from typing import List, Optional
from domain.entities.pago import Pago


class IPagoRepository(ABC):

    @abstractmethod
    def guardar(self, pago: Pago) -> Pago:
        """Persiste un pago realizado."""
        pass

    @abstractmethod
    def buscar_por_codigo(self, codigo: int) -> Optional[Pago]:
        """Busca un pago por su código identificador."""
        pass

    @abstractmethod
    def actualizar(self, pago: Pago) -> Pago:
        """Actualiza la información de un pago."""
        pass

    @abstractmethod
    def eliminar(self, codigo: int) -> bool:
        """Elimina el registro de un pago."""
        pass

    @abstractmethod
    def listar_todos(self) -> List[Pago]:
        """Retorna todos los pagos registrados."""
        pass

    @abstractmethod
    def listar_por_alquiler(self, alquiler_codigo: int) -> List[Pago]:
        """Recupera los pagos aplicados a un alquiler específico."""
        pass

    @abstractmethod
    def listar_por_negocio_cronologico(self, negocio_codigo: int) -> List[Pago]:
        """
        HU-08: Recupera la lista cronológica de pagos asociados a los alquileres
        de un negocio, ordenados por fecha de pago.
        """
        pass
