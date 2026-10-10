"""
=============================================================================
ENFOQUE 4: ARQUITECTURA HEXAGONAL (PUERTOS Y ADAPTADORES) + SOLID
=============================================================================
CARACTERÍSTICAS DEL ENFOQUE:
- El Dominio es el núcleo aislado (dentro del Hexágono). No conoce bases de
  datos, frameworks web, interfaces de usuario ni infraestructura externa.
- Puertos Primarios (Inbound / Driving Ports):
  Contratos que definen qué operaciones ofrece el hexágono al mundo exterior
  (Casos de uso / API del dominio).
- Puertos Secundarios (Outbound / Driven Ports):
  Contratos definidos por el dominio para que los adaptadores de infraestructura
  (repositorios, notificaciones) suministren lo que el dominio necesita.
- Inversión de Dependencias Radical (DIP de SOLID):
  Todas las dependencias apuntan HACIA EL CENTRO (hacia el hexágono).
  El dominio define las interfaces y la infraestructura las implementa.
- Máxima Testabilidad:
  El servicio de dominio se prueba al 100% conectando adaptadores en memoria
  sin necesidad de levantar bases de datos ni servicios externos.
=============================================================================
"""

from abc import ABC, abstractmethod
from datetime import date
from typing import Optional, List, Dict, Any


# ===========================================================================
# 1. DENTRO DEL HEXÁGONO: ENTIDADES Y VALUE OBJECTS DEL DOMINIO (Modelos Ricos)
# ===========================================================================
class PagoEntity:
    def __init__(self, codigo: int, alquiler_codigo: int, monto: float, fecha: date, tipo: str):
        if monto <= 0:
            raise ValueError("[Hexagonal Dominio] El monto debe ser mayor a 0.")
        self.codigo = codigo
        self.alquiler_codigo = alquiler_codigo
        self.monto = monto
        self.fecha = fecha
        self.tipo = tipo


class AlquilerEntity:
    def __init__(self, codigo: int, negocio_codigo: int, valor: float, fecha_limite: date):
        self.codigo = codigo
        self.negocio_codigo = negocio_codigo
        self.valor = valor
        self.fecha_limite = fecha_limite
        self._saldo = valor
        self._estado = "PENDIENTE"

    @property
    def saldo(self) -> float:
        return self._saldo

    @property
    def estado(self) -> str:
        return self._estado

    def aplicar_abono(self, monto: float) -> None:
        """Invariante del negocio: descuenta saldo y actualiza estado en tiempo real."""
        if monto > self._saldo:
            raise ValueError(f"[Hexagonal Dominio] El monto ({monto}) supera el saldo ({self._saldo}).")
        self._saldo = round(self._saldo - monto, 2)
        if self._saldo == 0.0:
            self._estado = "PAGADO"


class NegocioEntity:
    def __init__(self, codigo: int, nombre: str, ubicacion: str, estado: str = "ACTIVO"):
        self.codigo = codigo
        self.nombre = nombre
        self.ubicacion = ubicacion
        self.estado = estado


# ===========================================================================
# 2. DENTRO DEL HEXÁGONO: PUERTOS (PORTS - INTERFACES SOLID)
# ===========================================================================

# Puerto Primario (Driving / Inbound Port - API que consume la UI o el Controlador)
class IRegistrarPagoUseCase(ABC):
    """Puerto de Entrada: Caso de uso que expone el hexágono al mundo exterior."""
    @abstractmethod
    def registrar_pago(
        self,
        pago_codigo: int,
        negocio_codigo: int,
        alquiler_codigo: int,
        monto: float,
        tipo_pago: str
    ) -> Dict[str, Any]:
        pass


# Puertos Secundarios (Driven / Outbound Ports - Contratos de SPI que requiere el Dominio)
class INegocioRepositoryPort(ABC):
    @abstractmethod
    def buscar_por_codigo(self, codigo: int) -> Optional[NegocioEntity]: pass


class IAlquilerRepositoryPort(ABC):
    @abstractmethod
    def buscar_por_codigo(self, codigo: int) -> Optional[AlquilerEntity]: pass

    @abstractmethod
    def actualizar(self, alquiler: AlquilerEntity) -> None: pass


class IPagoRepositoryPort(ABC):
    @abstractmethod
    def guardar(self, pago: PagoEntity) -> None: pass


class INotificadorEventosPort(ABC):
    """Demuestra ISP: Puerto opcional para emitir eventos de dominio."""
    @abstractmethod
    def notificar_pago_exitoso(self, pago: PagoEntity, saldo_restante: float) -> None: pass


# ===========================================================================
# 3. DENTRO DEL HEXÁGONO: DOMAIN SERVICE (Implementa el Puerto Primario)
# ===========================================================================
class PagoDomainServiceHexagonal(IRegistrarPagoUseCase):
    """
    Servicio de Dominio en el corazón del Hexágono.
    - Implementa el Puerto Primario IRegistrarPagoUseCase.
    - Orquesta los agregados del dominio (Negocio, Alquiler, Pago).
    - Consume únicamente Puertos Secundarios (DIP total).
    - Desacoplado 100% de la infraestructura externa.
    """
    def __init__(
        self,
        negocio_repo: INegocioRepositoryPort,
        alquiler_repo: IAlquilerRepositoryPort,
        pago_repo: IPagoRepositoryPort,
        notificador: Optional[INotificadorEventosPort] = None
    ):
        self._negocio_repo = negocio_repo
        self._alquiler_repo = alquiler_repo
        self._pago_repo = pago_repo
        self._notificador = notificador

    def registrar_pago(
        self,
        pago_codigo: int,
        negocio_codigo: int,
        alquiler_codigo: int,
        monto: float,
        tipo_pago: str
    ) -> Dict[str, Any]:
        # HU-05: 1. Validar existencia del negocio mediante el puerto secundario
        negocio = self._negocio_repo.buscar_por_codigo(negocio_codigo)
        if not negocio:
            raise ValueError(f"[Hexagonal Dominio] El negocio {negocio_codigo} no existe.")

        # 2. Validar existencia del alquiler
        alquiler = self._alquiler_repo.buscar_por_codigo(alquiler_codigo)
        if not alquiler:
            raise ValueError(f"[Hexagonal Dominio] El alquiler {alquiler_codigo} no existe.")

        if alquiler.negocio_codigo != negocio_codigo:
            raise ValueError("[Hexagonal Dominio] El alquiler no corresponde al negocio especificado.")

        # 3. Operación de negocio delegada a la entidad rica (descuenta saldo en tiempo real)
        alquiler.aplicar_abono(monto)

        # 4. Crear entidad de pago y persistir a través de los puertos secundarios
        pago = PagoEntity(pago_codigo, alquiler_codigo, monto, date.today(), tipo_pago)
        self._pago_repo.guardar(pago)
        self._alquiler_repo.actualizar(alquiler)

        # 5. Notificación opcional (puerto secundario)
        if self._notificador:
            self._notificador.notificar_pago_exitoso(pago, alquiler.saldo)

        return {
            "pago_codigo": pago.codigo,
            "alquiler_codigo": alquiler.codigo,
            "negocio_codigo": negocio.codigo,
            "monto_pagado": pago.monto,
            "saldo_restante": alquiler.saldo,
            "nuevo_estado": alquiler.estado
        }


# ===========================================================================
# 4. FUERA DEL HEXÁGONO: ADAPTADORES (ADAPTERS)
# ===========================================================================

# Adaptadores Secundarios (Implementan los Outbound Ports)
class InMemoryNegocioRepositoryAdapter(INegocioRepositoryPort):
    def __init__(self, data: Dict[int, NegocioEntity]):
        self._data = data

    def buscar_por_codigo(self, codigo: int) -> Optional[NegocioEntity]:
        return self._data.get(codigo)


class InMemoryAlquilerRepositoryAdapter(IAlquilerRepositoryPort):
    def __init__(self, data: Dict[int, AlquilerEntity]):
        self._data = data

    def buscar_por_codigo(self, codigo: int) -> Optional[AlquilerEntity]:
        return self._data.get(codigo)

    def actualizar(self, alquiler: AlquilerEntity) -> None:
        self._data[alquiler.codigo] = alquiler


class InMemoryPagoRepositoryAdapter(IPagoRepositoryPort):
    def __init__(self):
        self._data: Dict[int, PagoEntity] = {}

    def guardar(self, pago: PagoEntity) -> None:
        self._data[pago.codigo] = pago


# Adaptador Primario (Driving Adapter: Controlador HTTP / GUI / CLI)
class RestApiPagoControllerAdapter:
    """Adaptador de entrada que recibe peticiones HTTP y usa el puerto primario."""
    def __init__(self, use_case: IRegistrarPagoUseCase):
        self._use_case = use_case

    def post(self, payload: Dict[str, Any]) -> Dict[str, Any]:
        """Simula la recepción de una solicitud JSON."""
        return self._use_case.registrar_pago(
            pago_codigo=payload["pago_codigo"],
            negocio_codigo=payload["negocio_codigo"],
            alquiler_codigo=payload["alquiler_codigo"],
            monto=payload["monto"],
            tipo_pago=payload["tipo_pago"]
        )
