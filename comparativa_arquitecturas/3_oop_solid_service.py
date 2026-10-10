"""
=============================================================================
ENFOQUE 3: ORIENTADO A OBJETOS PURO APLICANDO PRINCIPIOS SOLID
=============================================================================
CARACTERÍSTICAS DEL ENFOQUE:
- S (Single Responsibility): Cada clase tiene una sola razón de cambio.
    - Entidad Alquiler: Gestiona su saldo y estado.
    - IValidadorPago: Se encarga exclusivamente de las reglas de validación.
    - GestorPagosService: Orquesta la interacción entre objetos.
- O (Open/Closed): Abierto a extensión, cerrado a modificación.
    - Se pueden añadir nuevas estrategias de validación o políticas de cobro
      implementando la interfaz IValidadorPago sin tocar el servicio.
- L (Liskov Substitution):
    - Cualquier subtipo de IValidadorPago o de IPagable puede sustituir al tipo
      base sin alterar la corrección del programa.
- I (Interface Segregation):
    - Interfaces pequeñas y cohesivas (IPagable, IConsultadorDeuda, IPersistenciaPago)
      en lugar de interfaces gigantes.
- D (Dependency Inversion):
    - El servicio depende exclusivamente de abstracciones (interfaces), no de
      clases concretas ni de bases de datos.
=============================================================================
"""

from abc import ABC, abstractmethod
from datetime import date
from typing import Optional, List


# ---------------------------------------------------------------------------
# INTERFACES SEGREGADAS (I - Interface Segregation Principle)
# ---------------------------------------------------------------------------
class IPagable(ABC):
    """Contrato para cualquier elemento del dominio que reciba pagos."""
    @property
    @abstractmethod
    def saldo_pendiente(self) -> float: pass

    @abstractmethod
    def aplicar_abono(self, monto: float) -> None: pass


class IValidadorPago(ABC):
    """O - Open/Closed: Permite crear nuevas reglas de validación sin alterar el servicio."""
    @abstractmethod
    def validar(self, pagable: IPagable, monto: float) -> None: pass


class IConsultadorAlquiler(ABC):
    """D - Dependency Inversion: Interfaz específica para consulta de alquileres."""
    @abstractmethod
    def obtener_por_id(self, alquiler_id: int) -> Optional["AlquilerOOP"]: pass


class IGuardadorTransaccion(ABC):
    """D - Dependency Inversion: Interfaz específica para persistir pagos."""
    @abstractmethod
    def guardar_pago(self, pago: "PagoOOP") -> None: pass

    @abstractmethod
    def actualizar_alquiler(self, alquiler: "AlquilerOOP") -> None: pass


# ---------------------------------------------------------------------------
# OBJETOS DE DOMINIO RICOS (OOP con Encapsulamiento e Invariantes)
# ---------------------------------------------------------------------------
class PagoOOP:
    """Entidad Pago con identidad y atributos inmutables."""
    def __init__(self, id: int, alquiler_id: int, monto: float, fecha: date, tipo: str):
        if monto <= 0:
            raise ValueError("[OOP Error] El monto del pago debe ser estrictamente positivo.")
        self.id = id
        self.alquiler_id = alquiler_id
        self.monto = monto
        self.fecha = fecha
        self.tipo = tipo


class AlquilerOOP(IPagable):
    """Entidad que cumple el principio de responsabilidad única (S) y el contrato IPagable (L)."""
    def __init__(self, id: int, negocio_id: int, valor: float, fecha_limite: date):
        self.id = id
        self.negocio_id = negocio_id
        self.valor = valor
        self.fecha_limite = fecha_limite
        self._saldo = valor
        self._estado = "PENDIENTE"

    @property
    def saldo_pendiente(self) -> float:
        return self._saldo

    @property
    def estado(self) -> str:
        return self._estado

    def aplicar_abono(self, monto: float) -> None:
        """Encapsula la regla de modificación de saldo y cambio de estado."""
        if monto > self._saldo:
            raise ValueError(f"[OOP Error] El abono ({monto}) supera el saldo ({self._saldo}).")
        self._saldo = round(self._saldo - monto, 2)
        if self._saldo == 0:
            self._estado = "PAGADO"


# ---------------------------------------------------------------------------
# EXTENSIÓN POLIMÓRFICA (O - Open/Closed y S - Single Responsibility)
# ---------------------------------------------------------------------------
class ValidadorReglasFinancieras(IValidadorPago):
    """Validador estándar de pagos."""
    def validar(self, pagable: IPagable, monto: float) -> None:
        if monto <= 0:
            raise ValueError("[SOLID Validador] El monto debe ser mayor a cero.")
        if monto > pagable.saldo_pendiente:
            raise ValueError(f"[SOLID Validador] El monto excede el saldo pendiente ({pagable.saldo_pendiente}).")


# ---------------------------------------------------------------------------
# SERVICIO ORIENTADO A OBJETOS (D - Dependency Inversion)
# ---------------------------------------------------------------------------
class GestorPagosServiceOOP:
    """
    Servicio de Dominio con diseño OO puro y SOLID:
    - S: Coordina el ciclo de vida del pago.
    - O: Acepta cualquier lista de validadores polimórficos.
    - L: Funciona con cualquier objeto que satisfaga IPagable.
    - I: Solo depende de las interfaces específicas que necesita.
    - D: Desacoplado de la base de datos a través de abstracciones.
    """
    def __init__(
        self,
        consultador: IConsultadorAlquiler,
        guardador: IGuardadorTransaccion,
        validadores: Optional[List[IValidadorPago]] = None
    ):
        self._consultador = consultador
        self._guardador = guardador
        self._validadores = validadores or [ValidadorReglasFinancieras()]

    def ejecutar_pago(self, pago_id: int, alquiler_id: int, monto: float, tipo: str) -> PagoOOP:
        # 1. Recuperar objeto del dominio
        alquiler = self._consultador.obtener_por_id(alquiler_id)
        if not alquiler:
            raise ValueError(f"[OOP Error] Alquiler {alquiler_id} no encontrado.")

        # 2. Ejecutar validaciones polimórficas (Open/Closed)
        for validador in self._validadores:
            validador.validar(alquiler, monto)

        # 3. La propia entidad rica realiza el cambio de estado (No anémica)
        alquiler.aplicar_abono(monto)

        # 4. Crear el objeto de pago y persistir a través de abstracción
        pago = PagoOOP(pago_id, alquiler.id, monto, date.today(), tipo)
        self._guardador.guardar_pago(pago)
        self._guardador.actualizar_alquiler(alquiler)

        return pago
