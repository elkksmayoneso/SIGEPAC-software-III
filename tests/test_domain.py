"""
Suite de Pruebas Unitarias para la Capa de Dominio de SIGEPAC.
Valida el cumplimiento de RF1 a RF9, HU-03 a HU-08 y las invariantes de negocio.
Utiliza adaptadores en memoria (In-Memory Repositories) para demostrar la independencia de la arquitectura hexagonal.
"""

from datetime import date, timedelta
from typing import List, Optional, Dict
import unittest
import sys
from pathlib import Path

# Asegurar que la raíz del proyecto esté en sys.path independientemente de dónde se ejecute
ROOT_DIR = Path(__file__).resolve().parent.parent
if str(ROOT_DIR) not in sys.path:
    sys.path.insert(0, str(ROOT_DIR))

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
    UsuarioNoEsPropietarioException,
    NegocioNoEncontradoException,
    AlquilerNoEncontradoException,
    PagoExcedeSaldoException,
    MontoPagoInvalidoException,
    ReglaNegocioException,
)


# ==========================================
# ADAPTADORES EN MEMORIA (TEST DOUBLES / MOCKS)
# Demuestran que el dominio funciona 100% aislado
# ==========================================

class InMemoryUsuarioRepository(IUsuarioRepository):
    def __init__(self):
        self._db: Dict[int, Usuario] = {}

    def guardar(self, usuario: Usuario) -> Usuario:
        self._db[usuario.codigo] = usuario
        return usuario

    def buscar_por_codigo(self, codigo: int) -> Optional[Usuario]:
        return self._db.get(codigo)

    def buscar_por_correo(self, correo: str) -> Optional[Usuario]:
        for u in self._db.values():
            if u.correo.lower() == correo.lower():
                return u
        return None

    def actualizar(self, usuario: Usuario) -> Usuario:
        self._db[usuario.codigo] = usuario
        return usuario

    def eliminar(self, codigo: int) -> bool:
        return self._db.pop(codigo, None) is not None

    def listar_todos(self) -> List[Usuario]:
        return list(self._db.values())


class InMemoryNegocioRepository(INegocioRepository):
    def __init__(self):
        self._db: Dict[int, Negocio] = {}

    def guardar(self, negocio: Negocio) -> Negocio:
        self._db[negocio.codigo] = negocio
        return negocio

    def buscar_por_codigo(self, codigo: int) -> Optional[Negocio]:
        return self._db.get(codigo)

    def buscar_por_ubicacion(self, ubicacion: str) -> Optional[Negocio]:
        for n in self._db.values():
            if n.ubicacion.lower() == ubicacion.lower():
                return n
        return None

    def actualizar(self, negocio: Negocio) -> Negocio:
        self._db[negocio.codigo] = negocio
        return negocio

    def eliminar(self, codigo: int) -> bool:
        return self._db.pop(codigo, None) is not None

    def listar_todos(self) -> List[Negocio]:
        return list(self._db.values())

    def listar_por_propietario(self, propietario_codigo: int) -> List[Negocio]:
        return [n for n in self._db.values() if n.propietario_codigo == propietario_codigo]


class InMemoryAlquilerRepository(IAlquilerRepository):
    def __init__(self):
        self._db: Dict[int, Alquiler] = {}

    def guardar(self, alquiler: Alquiler) -> Alquiler:
        self._db[alquiler.codigo] = alquiler
        return alquiler

    def buscar_por_codigo(self, codigo: int) -> Optional[Alquiler]:
        return self._db.get(codigo)

    def actualizar(self, alquiler: Alquiler) -> Alquiler:
        self._db[alquiler.codigo] = alquiler
        return alquiler

    def eliminar(self, codigo: int) -> bool:
        return self._db.pop(codigo, None) is not None

    def listar_todos(self) -> List[Alquiler]:
        return list(self._db.values())

    def listar_por_negocio(self, negocio_codigo: int) -> List[Alquiler]:
        return [a for a in self._db.values() if a.negocio_codigo == negocio_codigo]

    def listar_pendientes(self) -> List[Alquiler]:
        return [a for a in self._db.values() if a.calcularSaldo() > 0]


class InMemoryPagoRepository(IPagoRepository):
    def __init__(self):
        self._db: Dict[int, Pago] = {}

    def guardar(self, pago: Pago) -> Pago:
        self._db[pago.codigo] = pago
        return pago

    def buscar_por_codigo(self, codigo: int) -> Optional[Pago]:
        return self._db.get(codigo)

    def actualizar(self, pago: Pago) -> Pago:
        self._db[pago.codigo] = pago
        return pago

    def eliminar(self, codigo: int) -> bool:
        return self._db.pop(codigo, None) is not None

    def listar_todos(self) -> List[Pago]:
        return list(self._db.values())

    def listar_por_alquiler(self, alquiler_codigo: int) -> List[Pago]:
        return [p for p in self._db.values() if p.alquiler_codigo == alquiler_codigo]

    def listar_por_negocio_cronologico(self, negocio_codigo: int) -> List[Pago]:
        # Para pruebas se puede devolver lista vacía o implementar según mapeo
        return []


class InMemoryMetodoPagoRepository(IMetodoPagoRepository):
    def __init__(self):
        self._db: Dict[int, MetodoPago] = {}

    def guardar(self, metodo: MetodoPago) -> MetodoPago:
        self._db[metodo.codigo_metodo] = metodo
        return metodo

    def buscar_por_codigo(self, codigo_metodo: int) -> Optional[MetodoPago]:
        return self._db.get(codigo_metodo)

    def listar_todos(self) -> List[MetodoPago]:
        return list(self._db.values())

    def eliminar(self, codigo_metodo: int) -> bool:
        return self._db.pop(codigo_metodo, None) is not None


# ==========================================
# CASOS DE PRUEBA
# ==========================================

class TestSigepacDomain(unittest.TestCase):

    def setUp(self):
        self.usuario_repo = InMemoryUsuarioRepository()
        self.negocio_repo = InMemoryNegocioRepository()
        self.alquiler_repo = InMemoryAlquilerRepository()
        self.pago_repo = InMemoryPagoRepository()
        self.metodo_repo = InMemoryMetodoPagoRepository()

        self.usuario_service = UsuarioService(self.usuario_repo, self.negocio_repo)
        self.negocio_service = NegocioService(self.negocio_repo, self.usuario_repo, self.alquiler_repo)
        self.alquiler_service = AlquilerService(self.alquiler_repo, self.negocio_repo, self.pago_repo)
        self.pago_service = PagoService(self.pago_repo, self.alquiler_repo, self.negocio_repo, self.metodo_repo)
        self.morosidad_service = MorosidadService(self.alquiler_repo, self.negocio_repo, self.pago_repo, self.usuario_repo)

        # Configurar métodos de pago iniciales
        self.metodo_repo.guardar(MetodoPago(1, "TRANSFERENCIA"))
        self.metodo_repo.guardar(MetodoPago(2, "EFECTIVO"))

    def test_rf1_a_rf4_gestion_usuarios_y_negocios(self):
        """RF1 a RF4: Registrar usuarios y negocios, editar y eliminar."""
        # 1. Registrar usuario propietario
        prop = self.usuario_service.registrar_usuario(
            codigo=1,
            nombre="Carlos",
            apellido="Gomez",
            correo="carlos@correo.com",
            contrasena="hashed_secret",
            tipo="PROPIETARIO",
            telefono="3001234567"
        )
        self.assertEqual(prop.nombre, "Carlos")
        self.assertTrue(prop.es_propietario())

        # 2. Editar información del usuario
        editado = self.usuario_service.editar_usuario(codigo=1, telefono="3009998888")
        self.assertEqual(editado.telefono, "3009998888")

        # 3. HU-03: Registrar negocio y asociar a propietario existente y local
        negocio = self.negocio_service.registrar_negocio(
            codigo=101,
            nombre="Café Plaza",
            ubicacion="Local A-12",
            propietario_codigo=1
        )
        self.assertEqual(negocio.nombre, "Café Plaza")
        self.assertEqual(negocio.propietario_codigo, 1)

        # 4. Validar que no se puede asociar a un usuario no propietario
        admin = self.usuario_service.registrar_usuario(
            codigo=2,
            nombre="Laura",
            apellido="Administradora",
            correo="admin@correo.com",
            contrasena="hashed_admin",
            tipo="ADMINISTRADORA",
            telefono="3110001122"
        )
        with self.assertRaises(UsuarioNoEsPropietarioException):
            self.negocio_service.registrar_negocio(
                codigo=102,
                nombre="Boutique Elegance",
                ubicacion="Local B-05",
                propietario_codigo=admin.codigo
            )

    def test_rf5_y_hu04_registrar_alquileres_asociados_al_negocio(self):
        """RF5 y HU-04: El valor del alquiler se asocia directamente al negocio."""
        # Setup: Propietario y Negocio
        self.usuario_service.registrar_usuario(
            codigo=10, nombre="Ana", apellido="Perez", correo="ana@correo.com",
            contrasena="pass", tipo="PROPIETARIO", telefono="123"
        )
        self.negocio_service.registrar_negocio(
            codigo=200, nombre="Restaurante Central", ubicacion="Local C-01",
            propietario_codigo=10
        )

        # Registrar alquiler
        hoy = date.today()
        limite = hoy + timedelta(days=15)
        alquiler = self.alquiler_service.registrar_alquiler(
            codigo=1001,
            negocio_codigo=200,
            fecha_inicio=hoy,
            fecha_limite=limite,
            valor=1500.0
        )

        self.assertEqual(alquiler.valor, 1500.0)
        self.assertEqual(alquiler.calcularSaldo(), 1500.0)
        self.assertEqual(alquiler.estado, EstadoAlquiler.PENDIENTE)

        # HU-07: Consultar estado de deuda del negocio
        estado_deuda = self.negocio_service.consultar_estado_deuda(200, fecha_referencia=hoy)
        self.assertEqual(estado_deuda["deuda_total_pendiente"], 1500.0)
        self.assertEqual(estado_deuda["deuda_vencida_mora"], 0.0)
        self.assertEqual(estado_deuda["estado_cuenta"], "CON_SALDO_PENDIENTE")

    def test_rf6_y_hu05_registro_pago_actualizacion_saldo_tiempo_real(self):
        """
        HU-05: Al registrar pago, verificar negocio existente y descontar saldo
        en tiempo real.
        """
        self.usuario_service.registrar_usuario(
            codigo=20, nombre="Mario", apellido="Ruiz", correo="mario@correo.com",
            contrasena="pass", tipo="PROPIETARIO", telefono="456"
        )
        self.negocio_service.registrar_negocio(
            codigo=300, nombre="Zapatería Real", ubicacion="Local D-10",
            propietario_codigo=20
        )

        hoy = date.today()
        self.alquiler_service.registrar_alquiler(
            codigo=2001, negocio_codigo=300, fecha_inicio=hoy,
            fecha_limite=hoy + timedelta(days=10), valor=1000.0
        )

        # 1. Error si el negocio no existe
        with self.assertRaises(NegocioNoEncontradoException):
            self.pago_service.registrar_pago(
                codigo=5001, negocio_codigo=9999, alquiler_codigo=2001,
                monto=500.0, fecha_pago=hoy, tipo_pago="EFECTIVO"
            )

        # 2. Registrar abono parcial de 400.0
        pago1 = self.pago_service.registrar_pago(
            codigo=5001, negocio_codigo=300, alquiler_codigo=2001,
            monto=400.0, fecha_pago=hoy, tipo_pago="TRANSFERENCIA", codigo_metodo=1
        )
        self.assertEqual(pago1.monto, 400.0)

        # Verificar saldo del alquiler en tiempo real
        alq_actualizado = self.alquiler_service.consultar_alquiler(2001)
        self.assertEqual(alq_actualizado.calcularSaldo(), 600.0)
        self.assertEqual(alq_actualizado.estado, EstadoAlquiler.PENDIENTE)

        # 3. Intentar sobrepagar más del saldo pendiente (debe fallar)
        with self.assertRaises(PagoExcedeSaldoException):
            self.pago_service.registrar_pago(
                codigo=5002, negocio_codigo=300, alquiler_codigo=2001,
                monto=700.0, fecha_pago=hoy, tipo_pago="EFECTIVO"
            )

        # 4. Registrar pago total restante (600.0)
        self.pago_service.registrar_pago(
            codigo=5003, negocio_codigo=300, alquiler_codigo=2001,
            monto=600.0, fecha_pago=hoy, tipo_pago="EFECTIVO", codigo_metodo=2
        )

        alq_final = self.alquiler_service.consultar_alquiler(2001)
        self.assertEqual(alq_final.calcularSaldo(), 0.0)
        self.assertEqual(alq_final.estado, EstadoAlquiler.PAGADO)

        # Estado de deuda del negocio queda en 0.0 y AL_DIA
        estado_deuda = self.negocio_service.consultar_estado_deuda(300, fecha_referencia=hoy)
        self.assertEqual(estado_deuda["deuda_total_pendiente"], 0.0)
        self.assertEqual(estado_deuda["estado_cuenta"], "AL_DIA")

    def test_hu06_y_rf9_servicio_de_morosidad(self):
        """
        HU-06 y RF9: Iterar sobre alquileres y filtrar aquellos con pagos pendientes
        y fecha vencida para retornar la lista de negocios en mora.
        """
        # Crear 2 propietarios y 2 negocios
        self.usuario_service.registrar_usuario(
            codigo=31, nombre="Lucía", apellido="Mendez", correo="lucia@correo.com",
            contrasena="pass", tipo="PROPIETARIO", telefono="777"
        )
        self.usuario_service.registrar_usuario(
            codigo=32, nombre="Roberto", apellido="Díaz", correo="roberto@correo.com",
            contrasena="pass", tipo="PROPIETARIO", telefono="888"
        )
        self.negocio_service.registrar_negocio(
            codigo=401, nombre="Panadería Sol", ubicacion="Local P-01", propietario_codigo=31
        )
        self.negocio_service.registrar_negocio(
            codigo=402, nombre="Librería Letras", ubicacion="Local L-02", propietario_codigo=32
        )

        hoy = date.today()
        hace_20_dias = hoy - timedelta(days=20)
        hace_5_dias = hoy - timedelta(days=5)
        en_15_dias = hoy + timedelta(days=15)

        # Negocio 401 tiene un alquiler vencido hace 5 días por 800 (en mora)
        self.alquiler_service.registrar_alquiler(
            codigo=3001, negocio_codigo=401, fecha_inicio=hace_20_dias,
            fecha_limite=hace_5_dias, valor=800.0
        )

        # Negocio 402 tiene un alquiler vigente no vencido (no está en mora)
        self.alquiler_service.registrar_alquiler(
            codigo=3002, negocio_codigo=402, fecha_inicio=hoy,
            fecha_limite=en_15_dias, valor=1200.0
        )

        # Consultar morosidad a la fecha de hoy
        negocios_en_mora = self.morosidad_service.obtener_negocios_en_mora(fecha_referencia=hoy)

        self.assertEqual(len(negocios_en_mora), 1)
        negocio_moroso = negocios_en_mora[0]
        self.assertEqual(negocio_moroso["negocio_codigo"], 401)
        self.assertEqual(negocio_moroso["negocio_nombre"], "Panadería Sol")
        self.assertEqual(negocio_moroso["total_deuda_en_mora"], 800.0)
        self.assertEqual(negocio_moroso["propietario"]["nombre_completo"], "Lucía Mendez")

    def test_hu08_y_rf8_historial_cronologico_pagos(self):
        """HU-08 y RF8: Historial cronológico con fechas y montos de pagos."""
        self.usuario_service.registrar_usuario(
            codigo=50, nombre="Elena", apellido="Torres", correo="elena@correo.com",
            contrasena="pass", tipo="PROPIETARIO", telefono="999"
        )
        self.negocio_service.registrar_negocio(
            codigo=500, nombre="Farmacia Vida", ubicacion="Local F-03", propietario_codigo=50
        )

        hoy = date.today()
        self.alquiler_service.registrar_alquiler(
            codigo=4001, negocio_codigo=500, fecha_inicio=hoy - timedelta(days=30),
            fecha_limite=hoy + timedelta(days=30), valor=2000.0
        )

        # Registrar pagos en diferentes fechas
        fecha1 = hoy - timedelta(days=10)
        fecha2 = hoy - timedelta(days=2)
        self.pago_service.registrar_pago(
            codigo=6001, negocio_codigo=500, alquiler_codigo=4001,
            monto=500.0, fecha_pago=fecha1, tipo_pago="TRANSFERENCIA"
        )
        self.pago_service.registrar_pago(
            codigo=6002, negocio_codigo=500, alquiler_codigo=4001,
            monto=700.0, fecha_pago=fecha2, tipo_pago="EFECTIVO"
        )

        historial = self.pago_service.obtener_historial_cronologico(negocio_codigo=500, ascendente=True)
        self.assertEqual(len(historial), 2)
        self.assertEqual(historial[0]["codigo_pago"], 6001)
        self.assertEqual(historial[0]["monto"], 500.0)
        self.assertEqual(historial[1]["codigo_pago"], 6002)
        self.assertEqual(historial[1]["monto"], 700.0)


if __name__ == "__main__":
    unittest.main()
