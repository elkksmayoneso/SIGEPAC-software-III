"""
Módulo de Excepciones del Dominio para SIGEPAC.
Define los errores de negocio y validación que pueden ocurrir en la capa de dominio.
No depende de frameworks ni infraestructura externa.
"""


class DomainException(Exception):
    """Clase base para todas las excepciones del dominio SIGEPAC."""
    pass


class EntidadNoEncontradaException(DomainException):
    """Se lanza cuando una entidad buscada no existe en el sistema."""
    pass


class UsuarioNoEncontradoException(EntidadNoEncontradaException):
    def __init__(self, codigo: int):
        super().__init__(f"No se encontró el usuario con código {codigo}.")


class NegocioNoEncontradoException(EntidadNoEncontradaException):
    def __init__(self, codigo: int):
        super().__init__(f"No se encontró el negocio con código {codigo}.")


class AlquilerNoEncontradoException(EntidadNoEncontradaException):
    def __init__(self, codigo: int):
        super().__init__(f"No se encontró el alquiler con código {codigo}.")


class MetodoPagoNoEncontradoException(EntidadNoEncontradaException):
    def __init__(self, codigo_metodo: int):
        super().__init__(f"No se encontró el método de pago con código {codigo_metodo}.")


class ReglaNegocioException(DomainException):
    """Se lanza cuando una operación viola una regla o invariante de negocio."""
    pass


class DatosInvalidosException(ReglaNegocioException):
    """Se lanza cuando los datos suministrados a una entidad o servicio son inválidos."""
    pass


class UsuarioNoEsPropietarioException(ReglaNegocioException):
    def __init__(self, codigo: int, tipo: str):
        super().__init__(f"El usuario con código {codigo} tiene tipo '{tipo}' y debe ser 'PROPIETARIO' para asociarse a un negocio.")


class MontoPagoInvalidoException(ReglaNegocioException):
    def __init__(self, monto: float):
        super().__init__(f"El monto de pago debe ser mayor a cero. Monto recibido: {monto}")


class PagoExcedeSaldoException(ReglaNegocioException):
    def __init__(self, monto: float, saldo_pendiente: float):
        super().__init__(f"El monto del pago ({monto}) excede el saldo pendiente del alquiler ({saldo_pendiente}).")


class AlquilerYaPagadoException(ReglaNegocioException):
    def __init__(self, codigo_alquiler: int):
        super().__init__(f"El alquiler con código {codigo_alquiler} ya se encuentra totalmente PAGADO.")
