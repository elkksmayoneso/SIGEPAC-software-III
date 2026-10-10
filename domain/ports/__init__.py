"""
Exportación de Puertos (Interfaces de Repositorios) del Dominio para SIGEPAC.
"""

from domain.ports.usuario_repository import IUsuarioRepository
from domain.ports.negocio_repository import INegocioRepository
from domain.ports.alquiler_repository import IAlquilerRepository
from domain.ports.pago_repository import IPagoRepository
from domain.ports.metodo_pago_repository import IMetodoPagoRepository

__all__ = [
    "IUsuarioRepository",
    "INegocioRepository",
    "IAlquilerRepository",
    "IPagoRepository",
    "IMetodoPagoRepository",
]
