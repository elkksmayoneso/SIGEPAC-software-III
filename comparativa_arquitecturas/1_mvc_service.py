"""
=============================================================================
ENFOQUE 1: MODELO-VISTA-CONTROLADOR (MVC)
=============================================================================
CARACTERÍSTICAS DEL ENFOQUE:
- En MVC tradicional (estilo ActiveRecord / Django / Rails), los modelos
  representan directamente las tablas de la base de datos y contienen métodos
  de persistencia (save, update, delete).
- El "Service" en MVC actúa como un "Model Service" o script transaccional que
  orquesta operaciones manipulando directamente los modelos de datos y la BD.
- No existe el principio de inversión de dependencias (DIP): el servicio
  depende directamente del ORM / base de datos concreta.
- El controlador recibe la petición, invoca al servicio/modelo y selecciona
  la vista para representar el resultado.
=============================================================================
"""

from datetime import date
from typing import Dict, Any, Optional


# ---------------------------------------------------------------------------
# 1. MODELOS (M en MVC - Típicamente ActiveRecord con persistencia integrada)
# ---------------------------------------------------------------------------
class NegocioModel:
    """Modelo acoplado a la tabla 'negocios' de la base de datos."""
    def __init__(self, id: int, nombre: str, ubicacion: str, estado: str):
        self.id = id
        self.nombre = nombre
        self.ubicacion = ubicacion
        self.estado = estado

    @classmethod
    def find_by_id(cls, db_session: Dict[str, Any], negocio_id: int) -> Optional["NegocioModel"]:
        """Simula una consulta directa SQL: SELECT * FROM negocios WHERE id = ?"""
        raw = db_session.get("negocios", {}).get(negocio_id)
        if raw:
            return cls(raw["id"], raw["nombre"], raw["ubicacion"], raw["estado"])
        return None


class AlquilerModel:
    """Modelo acoplado a la tabla 'alquileres'."""
    def __init__(self, id: int, negocio_id: int, valor: float, saldo: float, fecha_limite: date, estado: str):
        self.id = id
        self.negocio_id = negocio_id
        self.valor = valor
        self.saldo = saldo
        self.fecha_limite = fecha_limite
        self.estado = estado

    @classmethod
    def find_by_id(cls, db_session: Dict[str, Any], alquiler_id: int) -> Optional["AlquilerModel"]:
        """Consulta directa SQL: SELECT * FROM alquileres WHERE id = ?"""
        raw = db_session.get("alquileres", {}).get(alquiler_id)
        if raw:
            return cls(raw["id"], raw["negocio_id"], raw["valor"], raw["saldo"], raw["fecha_limite"], raw["estado"])
        return None

    def save(self, db_session: Dict[str, Any]) -> None:
        """Persistencia directa (ActiveRecord): UPDATE alquileres SET saldo=?, estado=? WHERE id=?"""
        db_session.setdefault("alquileres", {})[self.id] = {
            "id": self.id,
            "negocio_id": self.negocio_id,
            "valor": self.valor,
            "saldo": self.saldo,
            "fecha_limite": self.fecha_limite,
            "estado": self.estado
        }


class PagoModel:
    """Modelo acoplado a la tabla 'pagos'."""
    def __init__(self, id: int, alquiler_id: int, monto: float, fecha_pago: date, tipo_pago: str):
        self.id = id
        self.alquiler_id = alquiler_id
        self.monto = monto
        self.fecha_pago = fecha_pago
        self.tipo_pago = tipo_pago

    def save(self, db_session: Dict[str, Any]) -> None:
        """Persistencia directa: INSERT INTO pagos VALUES (?, ?, ?, ?, ?)"""
        db_session.setdefault("pagos", {})[self.id] = {
            "id": self.id,
            "alquiler_id": self.alquiler_id,
            "monto": self.monto,
            "fecha_pago": self.fecha_pago,
            "tipo_pago": self.tipo_pago
        }


# ---------------------------------------------------------------------------
# 2. SERVICIO EN MVC (Model Service / Business Helper)
# ---------------------------------------------------------------------------
class PagoServiceMVC:
    """
    Servicio de negocio dentro del ecosistema MVC.
    - Acoplado directamente a la sesión/conexión de Base de Datos.
    - Trabaja directamente con los Models de ActiveRecord.
    """
    def __init__(self, db_session: Dict[str, Any]):
        self.db_session = db_session  # Conexión directa a la BD

    def registrar_pago(self, pago_id: int, negocio_id: int, alquiler_id: int, monto: float, tipo_pago: str) -> Dict[str, Any]:
        # 1. Consulta directa al modelo Negocio
        negocio = NegocioModel.find_by_id(self.db_session, negocio_id)
        if not negocio:
            raise ValueError(f"[MVC Error] El negocio con ID {negocio_id} no existe.")

        # 2. Consulta directa al modelo Alquiler
        alquiler = AlquilerModel.find_by_id(self.db_session, alquiler_id)
        if not alquiler:
            raise ValueError(f"[MVC Error] El alquiler con ID {alquiler_id} no existe.")

        if alquiler.negocio_id != negocio_id:
            raise ValueError("[MVC Error] El alquiler no pertenece al negocio especificado.")

        if monto <= 0:
            raise ValueError("[MVC Error] El monto debe ser mayor a cero.")

        if monto > alquiler.saldo:
            raise ValueError(f"[MVC Error] El monto ({monto}) excede el saldo pendiente ({alquiler.saldo}).")

        # 3. Lógica transaccional y actualización directa de modelos
        alquiler.saldo = round(alquiler.saldo - monto, 2)
        alquiler.estado = "PAGADO" if alquiler.saldo == 0 else "PENDIENTE"
        alquiler.save(self.db_session)

        # 4. Creación y guardado del pago
        pago = PagoModel(pago_id, alquiler_id, monto, date.today(), tipo_pago)
        pago.save(self.db_session)

        return {
            "pago_id": pago.id,
            "alquiler_id": alquiler.id,
            "saldo_restante": alquiler.saldo,
            "estado_alquiler": alquiler.estado
        }


# ---------------------------------------------------------------------------
# 3. CONTROLADOR Y VISTA (C y V en MVC)
# ---------------------------------------------------------------------------
class PagoControllerMVC:
    """Controlador que recibe peticiones del usuario/HTTP y delega al servicio."""
    def __init__(self, service: PagoServiceMVC, vista: "PagoViewMVC"):
        self.service = service
        self.vista = vista

    def post_registrar_pago(self, request_data: Dict[str, Any]) -> str:
        try:
            resultado = self.service.registrar_pago(
                pago_id=request_data["pago_id"],
                negocio_id=request_data["negocio_id"],
                alquiler_id=request_data["alquiler_id"],
                monto=request_data["monto"],
                tipo_pago=request_data["tipo_pago"]
            )
            return self.vista.render_success(resultado)
        except Exception as e:
            return self.vista.render_error(str(e))


class PagoViewMVC:
    """Vista que formatea la salida para el cliente."""
    def render_success(self, data: Dict[str, Any]) -> str:
        return f"[MVC View - 200 OK]: Pago #{data['pago_id']} registrado. Nuevo saldo: ${data['saldo_restante']} ({data['estado_alquiler']})"

    def render_error(self, error_msg: str) -> str:
        return f"[MVC View - 400 Bad Request]: {error_msg}"
