"""
Script de Demostración y Comparación de las 4 Arquitecturas para SIGEPAC.
Ejecuta el mismo caso de uso (registrar un pago de $400 a una deuda de $1000)
en los 4 enfoques arquitectónicos para verificar su funcionamiento.
"""

from datetime import date

# 1. Importar Enfoque MVC
import importlib.util
from pathlib import Path

BASE = Path(__file__).parent


def cargar_modulo(nombre: str, ruta_archivo: Path):
    spec = importlib.util.spec_from_file_location(nombre, ruta_archivo)
    mod = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(mod)
    return mod


mod_mvc = cargar_modulo("mod_mvc", BASE / "1_mvc_service.py")
mod_layered = cargar_modulo("mod_layered", BASE / "2_layered_service.py")
mod_oop = cargar_modulo("mod_oop", BASE / "3_oop_solid_service.py")
mod_hex = cargar_modulo("mod_hex", BASE / "4_hexagonal_solid_service.py")


def demo_mvc():
    print("\n" + "=" * 70)
    print(">>> 1. DEMO: MODELO-VISTA-CONTROLADOR (MVC)")
    print("=" * 70)
    # Simulación de BD relacional
    db_session = {
        "negocios": {101: {"id": 101, "nombre": "Café Plaza", "ubicacion": "Local A-12", "estado": "ACTIVO"}},
        "alquileres": {501: {"id": 501, "negocio_id": 101, "valor": 1000.0, "saldo": 1000.0, "fecha_limite": date.today(), "estado": "PENDIENTE"}},
        "pagos": {}
    }
    service = mod_mvc.PagoServiceMVC(db_session)
    view = mod_mvc.PagoViewMVC()
    controller = mod_mvc.PagoControllerMVC(service, view)

    response = controller.post_registrar_pago({
        "pago_id": 9001,
        "negocio_id": 101,
        "alquiler_id": 501,
        "monto": 400.0,
        "tipo_pago": "TRANSFERENCIA"
    })
    print(response)


def demo_layered():
    print("\n" + "=" * 70)
    print(">>> 2. DEMO: ARQUITECTURA EN CAPAS (N-TIER)")
    print("=" * 70)
    db = {
        "negocios": {101: {"id": 101, "nombre": "Café Plaza", "ubicacion": "Local A-12", "estado": "ACTIVO"}},
        "alquileres": {501: {"id": 501, "negocio_id": 101, "valor": 1000.0, "saldo": 1000.0, "fecha_limite": date.today(), "estado": "PENDIENTE"}},
        "pagos": {}
    }
    negocio_dao = mod_layered.NegocioDAO(db)
    alquiler_dao = mod_layered.AlquilerDAO(db)
    pago_dao = mod_layered.PagoDAO(db)

    service = mod_layered.PagoBusinessService(negocio_dao, alquiler_dao, pago_dao)
    pago = service.procesar_pago(
        pago_id=9002, negocio_id=101, alquiler_id=501, monto=400.0, tipo_pago="EFECTIVO"
    )
    print(f"[Layered Result]: Pago registrado ID #{pago.id}, Monto: ${pago.monto}. Saldo en BD: ${db['alquileres'][501]['saldo']}")


def demo_oop_solid():
    print("\n" + "=" * 70)
    print(">>> 3. DEMO: ORIENTADA A OBJETOS PURA + SOLID")
    print("=" * 70)
    # Implementación mock de las abstracciones
    alquiler_obj = mod_oop.AlquilerOOP(501, 101, 1000.0, date.today())
    pagos_almacenados = {}

    class MockConsultador(mod_oop.IConsultadorAlquiler):
        def obtener_por_id(self, alquiler_id: int):
            return alquiler_obj if alquiler_id == 501 else None

    class MockGuardador(mod_oop.IGuardadorTransaccion):
        def guardar_pago(self, pago):
            pagos_almacenados[pago.id] = pago

        def actualizar_alquiler(self, alquiler):
            pass

    service = mod_oop.GestorPagosServiceOOP(MockConsultador(), MockGuardador())
    pago = service.ejecutar_pago(pago_id=9003, alquiler_id=501, monto=400.0, tipo="TRANSFERENCIA")
    print(f"[OOP SOLID Result]: Pago #{pago.id} creado. Nuevo saldo de la entidad: ${alquiler_obj.saldo_pendiente} ({alquiler_obj.estado})")


def demo_hexagonal_solid():
    print("\n" + "=" * 70)
    print(">>> 4. DEMO: ARQUITECTURA HEXAGONAL + SOLID")
    print("=" * 70)
    negocio_entity = mod_hex.NegocioEntity(101, "Café Plaza", "Local A-12")
    alquiler_entity = mod_hex.AlquilerEntity(501, 101, 1000.0, date.today())

    # Adaptadores secundarios (Driven Adapters)
    negocio_repo = mod_hex.InMemoryNegocioRepositoryAdapter({101: negocio_entity})
    alquiler_repo = mod_hex.InMemoryAlquilerRepositoryAdapter({501: alquiler_entity})
    pago_repo = mod_hex.InMemoryPagoRepositoryAdapter()

    # Servicio de Dominio (Núcleo Hexagonal implementando el Puerto Primario)
    domain_service = mod_hex.PagoDomainServiceHexagonal(negocio_repo, alquiler_repo, pago_repo)

    # Adaptador Primario (Driving Adapter)
    api_controller = mod_hex.RestApiPagoControllerAdapter(domain_service)

    resultado = api_controller.post({
        "pago_codigo": 9004,
        "negocio_codigo": 101,
        "alquiler_codigo": 501,
        "monto": 400.0,
        "tipo_pago": "TRANSFERENCIA"
    })
    print(f"[Hexagonal Result]: {resultado}")


if __name__ == "__main__":
    demo_mvc()
    demo_layered()
    demo_oop_solid()
    demo_hexagonal_solid()
