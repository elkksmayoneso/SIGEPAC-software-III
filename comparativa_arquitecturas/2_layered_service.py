"""
=============================================================================
ENFOQUE 2: ARQUITECTURA EN CAPAS (LAYERED / N-TIER TRADICIONAL)
=============================================================================
CARACTERÍSTICAS DEL ENFOQUE:
- Estructura clásica en 3 o N capas:
    Capa de Presentación (UI / API)
        ↓ (depende de)
    Capa de Lógica de Negocio (Business Service)
        ↓ (depende de)
    Capa de Acceso a Datos (DAO / Data Access Layer)
- Flujo de Dependencias "Top-Down" (de arriba hacia abajo):
  La capa de negocio DEPENDE DIRECTAMENTE de las implementaciones concretas
  de acceso a datos (NegocioDAO, AlquilerDAO, PagoDAO).
- VIOLACIÓN DEL PRINCIPIO SOLID 'DIP' (Dependency Inversion Principle):
  Los módulos de alto nivel (negocio) dependen directamente de módulos de bajo
  nivel (acceso a base de datos).
- Modelos Anémicos: Las entidades suelen ser meras bolsas de datos (DTOs o
  estructuras sin comportamiento). La lógica reside 100% en el Business Service.
=============================================================================
"""

from datetime import date
from typing import Optional, Dict, Any


# ---------------------------------------------------------------------------
# CAPA 1: MODELOS DE DATOS ANÉMICOS (Data Transfer Objects / Entidades pasivas)
# ---------------------------------------------------------------------------
class NegocioDTO:
    def __init__(self, id: int, nombre: str, ubicacion: str, estado: str):
        self.id = id
        self.nombre = nombre
        self.ubicacion = ubicacion
        self.estado = estado


class AlquilerDTO:
    def __init__(self, id: int, negocio_id: int, valor: float, saldo: float, fecha_limite: date, estado: str):
        self.id = id
        self.negocio_id = negocio_id
        self.valor = valor
        self.saldo = saldo
        self.fecha_limite = fecha_limite
        self.estado = estado


class PagoDTO:
    def __init__(self, id: int, alquiler_id: int, monto: float, fecha_pago: date, tipo_pago: str):
        self.id = id
        self.alquiler_id = alquiler_id
        self.monto = monto
        self.fecha_pago = fecha_pago
        self.tipo_pago = tipo_pago


# ---------------------------------------------------------------------------
# CAPA 2: ACCESO A DATOS (Data Access Layer - DAO Concretos)
# ---------------------------------------------------------------------------
class NegocioDAO:
    """Acceso a datos concreto para la tabla Negocios."""
    def __init__(self, db_conexion: Dict[str, Any]):
        self.db = db_conexion

    def get_by_id(self, negocio_id: int) -> Optional[NegocioDTO]:
        data = self.db.get("negocios", {}).get(negocio_id)
        if data:
            return NegocioDTO(data["id"], data["nombre"], data["ubicacion"], data["estado"])
        return None


class AlquilerDAO:
    """Acceso a datos concreto para la tabla Alquileres."""
    def __init__(self, db_conexion: Dict[str, Any]):
        self.db = db_conexion

    def get_by_id(self, alquiler_id: int) -> Optional[AlquilerDTO]:
        data = self.db.get("alquileres", {}).get(alquiler_id)
        if data:
            return AlquilerDTO(data["id"], data["negocio_id"], data["valor"], data["saldo"], data["fecha_limite"], data["estado"])
        return None

    def update_saldo_y_estado(self, alquiler_id: int, nuevo_saldo: float, nuevo_estado: str) -> None:
        if alquiler_id in self.db.get("alquileres", {}):
            self.db["alquileres"][alquiler_id]["saldo"] = nuevo_saldo
            self.db["alquileres"][alquiler_id]["estado"] = nuevo_estado


class PagoDAO:
    """Acceso a datos concreto para la tabla Pagos."""
    def __init__(self, db_conexion: Dict[str, Any]):
        self.db = db_conexion

    def insert(self, pago: PagoDTO) -> None:
        self.db.setdefault("pagos", {})[pago.id] = {
            "id": pago.id,
            "alquiler_id": pago.alquiler_id,
            "monto": pago.monto,
            "fecha_pago": pago.fecha_pago,
            "tipo_pago": pago.tipo_pago
        }


# ---------------------------------------------------------------------------
# CAPA 3: LÓGICA DE NEGOCIO (Business Logic Layer - Business Service)
# ---------------------------------------------------------------------------
class PagoBusinessService:
    """
    Servicio de la Capa de Negocio tradicional.
    Depende directamente de los DAOs concretos (acoplamiento hacia abajo).
    Contiene toda la lógica de negocio porque las entidades son anémicas.
    """
    def __init__(self, negocio_dao: NegocioDAO, alquiler_dao: AlquilerDAO, pago_dao: PagoDAO):
        # Acoplamiento directo a clases concretas (sin interfaces intermedias)
        self.negocio_dao = negocio_dao
        self.alquiler_dao = alquiler_dao
        self.pago_dao = pago_dao

    def procesar_pago(self, pago_id: int, negocio_id: int, alquiler_id: int, monto: float, tipo_pago: str) -> PagoDTO:
        # 1. Validar negocio
        negocio = self.negocio_dao.get_by_id(negocio_id)
        if not negocio:
            raise Exception(f"[Layered Error] Negocio {negocio_id} no encontrado en la base de datos.")

        # 2. Validar alquiler
        alquiler = self.alquiler_dao.get_by_id(alquiler_id)
        if not alquiler:
            raise Exception(f"[Layered Error] Alquiler {alquiler_id} no encontrado.")

        if alquiler.negocio_id != negocio_id:
            raise Exception("[Layered Error] El alquiler no corresponde al negocio.")

        # 3. Lógica de negocio (calculada aquí porque AlquilerDTO es anémico)
        if monto <= 0:
            raise Exception("[Layered Error] El monto a pagar debe ser positivo.")

        if monto > alquiler.saldo:
            raise Exception(f"[Layered Error] Monto {monto} excede la deuda de {alquiler.saldo}.")

        nuevo_saldo = round(alquiler.saldo - monto, 2)
        nuevo_estado = "PAGADO" if nuevo_saldo == 0 else "PENDIENTE"

        # 4. Actualizar estado y registrar en la capa de datos
        self.alquiler_dao.update_saldo_y_estado(alquiler_id, nuevo_saldo, nuevo_estado)

        nuevo_pago = PagoDTO(pago_id, alquiler_id, monto, date.today(), tipo_pago)
        self.pago_dao.insert(nuevo_pago)

        return nuevo_pago
