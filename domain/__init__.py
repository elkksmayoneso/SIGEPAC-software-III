"""
Capa de Dominio del Sistema SIGEPAC.
Implementación bajo Arquitectura Hexagonal y Domain-Driven Design (DDD).
"""

from domain.entities import (
    Usuario,
    Negocio,
    EstadoNegocio,
    Alquiler,
    EstadoAlquiler,
    Pago,
    MetodoPago,
)
from domain.ports import (
    IUsuarioRepository,
    INegocioRepository,
    IAlquilerRepository,
    IPagoRepository,
    IMetodoPagoRepository,
)
from domain.services import (
    UsuarioService,
    NegocioService,
    AlquilerService,
    PagoService,
    MorosidadService,
)
from domain.exceptions import (
    DomainException,
    EntidadNoEncontradaException,
    UsuarioNoEncontradoException,
    NegocioNoEncontradoException,
    AlquilerNoEncontradoException,
    MetodoPagoNoEncontradoException,
    ReglaNegocioException,
    DatosInvalidosException,
    UsuarioNoEsPropietarioException,
    MontoPagoInvalidoException,
    PagoExcedeSaldoException,
    AlquilerYaPagadoException,
)

__all__ = [
    # Entidades
    "Usuario",
    "Negocio",
    "EstadoNegocio",
    "Alquiler",
    "EstadoAlquiler",
    "Pago",
    "MetodoPago",
    # Puertos
    "IUsuarioRepository",
    "INegocioRepository",
    "IAlquilerRepository",
    "IPagoRepository",
    "IMetodoPagoRepository",
    # Servicios
    "UsuarioService",
    "NegocioService",
    "AlquilerService",
    "PagoService",
    "MorosidadService",
    # Excepciones
    "DomainException",
    "EntidadNoEncontradaException",
    "UsuarioNoEncontradoException",
    "NegocioNoEncontradoException",
    "AlquilerNoEncontradoException",
    "MetodoPagoNoEncontradoException",
    "ReglaNegocioException",
    "DatosInvalidosException",
    "UsuarioNoEsPropietarioException",
    "MontoPagoInvalidoException",
    "PagoExcedeSaldoException",
    "AlquilerYaPagadoException",
]
