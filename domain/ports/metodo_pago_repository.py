"""
Puerto Secundario (Driven Port): IMetodoPagoRepository
Define el contrato para gestionar los métodos de pago disponibles en el sistema.
"""

from abc import ABC, abstractmethod
from typing import List, Optional
from domain.entities.metodo_pago import MetodoPago


class IMetodoPagoRepository(ABC):

    @abstractmethod
    def guardar(self, metodo: MetodoPago) -> MetodoPago:
        """Persiste un método de pago."""
        pass

    @abstractmethod
    def buscar_por_codigo(self, codigo_metodo: int) -> Optional[MetodoPago]:
        """Busca un método de pago por su código identificador."""
        pass

    @abstractmethod
    def listar_todos(self) -> List[MetodoPago]:
        """Obtiene el catálogo de métodos de pago configurados."""
        pass

    @abstractmethod
    def eliminar(self, codigo_metodo: int) -> bool:
        """Elimina un método de pago."""
        pass
