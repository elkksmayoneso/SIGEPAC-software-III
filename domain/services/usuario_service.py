"""
Servicio de Dominio: UsuarioService
Implementa los Requisitos Funcionales RF1 a RF4 para la gestión de usuarios (Administradora / Propietarios).
Totalmente desacoplado de frameworks y persistencia concreta mediante IUsuarioRepository.
"""

from typing import List, Optional
from domain.entities.usuario import Usuario
from domain.ports.usuario_repository import IUsuarioRepository
from domain.ports.negocio_repository import INegocioRepository
from domain.exceptions import (
    UsuarioNoEncontradoException,
    DatosInvalidosException,
    ReglaNegocioException,
)


class UsuarioService:
    def __init__(
        self,
        usuario_repo: IUsuarioRepository,
        negocio_repo: Optional[INegocioRepository] = None
    ):
        self._usuario_repo = usuario_repo
        self._negocio_repo = negocio_repo

    def registrar_usuario(
        self,
        codigo: int,
        nombre: str,
        apellido: str,
        correo: str,
        contrasena: str,
        tipo: str,
        telefono: str
    ) -> Usuario:
        """RF1: Registrar un nuevo usuario validando unicidad de correo y código."""
        if self._usuario_repo.buscar_por_codigo(codigo):
            raise ReglaNegocioException(f"Ya existe un usuario con el código {codigo}.")

        if self._usuario_repo.buscar_por_correo(correo):
            raise ReglaNegocioException(f"El correo '{correo}' ya se encuentra registrado.")

        usuario = Usuario(
            codigo=codigo,
            nombre=nombre,
            apellido=apellido,
            correo=correo,
            contrasena=contrasena,
            tipo=tipo,
            telefono=telefono
        )
        return self._usuario_repo.guardar(usuario)

    def editar_usuario(
        self,
        codigo: int,
        nombre: Optional[str] = None,
        apellido: Optional[str] = None,
        correo: Optional[str] = None,
        telefono: Optional[str] = None
    ) -> Usuario:
        """RF2: Editar la información del usuario."""
        usuario = self._usuario_repo.buscar_por_codigo(codigo)
        if not usuario:
            raise UsuarioNoEncontradoException(codigo)

        if correo and correo.lower() != usuario.correo:
            existente = self._usuario_repo.buscar_por_correo(correo)
            if existente and existente.codigo != codigo:
                raise ReglaNegocioException(f"El correo '{correo}' ya pertenece a otro usuario.")

        usuario.actualizar_datos(
            nombre=nombre,
            apellido=apellido,
            correo=correo,
            telefono=telefono
        )
        return self._usuario_repo.actualizar(usuario)

    def eliminar_usuario(self, codigo: int) -> bool:
        """
        RF3/RF4: Elimina un usuario.
        Regla de integridad: No se puede eliminar si tiene negocios asociados activos.
        """
        usuario = self._usuario_repo.buscar_por_codigo(codigo)
        if not usuario:
            raise UsuarioNoEncontradoException(codigo)

        if self._negocio_repo:
            negocios_asociados = self._negocio_repo.listar_por_propietario(codigo)
            if negocios_asociados:
                raise ReglaNegocioException(
                    f"No se puede eliminar el usuario {codigo} porque tiene {len(negocios_asociados)} negocio(s) asociado(s)."
                )

        return self._usuario_repo.eliminar(codigo)

    def consultar_usuario(self, codigo: int) -> Usuario:
        """Consulta usuario por código."""
        usuario = self._usuario_repo.buscar_por_codigo(codigo)
        if not usuario:
            raise UsuarioNoEncontradoException(codigo)
        return usuario

    def listar_usuarios(self) -> List[Usuario]:
        """Lista todos los usuarios del sistema."""
        return self._usuario_repo.listar_todos()
