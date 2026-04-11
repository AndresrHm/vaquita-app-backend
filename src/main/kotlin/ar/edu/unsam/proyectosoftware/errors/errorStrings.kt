package ar.edu.unsam.proyectosoftware.errors

object ErrorStrings {
    const val MONTO_NEGATIVO = "El monto del gasto debe ser mayor a cero."
    const val DESCRIPCION_VACIA = "La descripción no puede estar vacía."
    const val PAGADOR_NO_PERTENECE_AL_GRUPO = "El usuario pagador no pertenece al grupo seleccionado."
    const val USUARIO_SIN_PERMISO_GASTO = "El usuario no tiene permisos para registrar gastos en este grupo."
    const val USUARIO_NO_ENCONTRADO = "El usuario no ha sido encontrado."
    const val GRUPO_NO_ENCONTRADO = "El grupo no ha sido encontrado."
    const val NINGUN_USUARIO_ENCONTRADO = "Uno o más elementos no fueron encontrados"
    const val PARTICIPANTES_NO_PERTENECEN_AL_GRUPO = "todos los usuarios participantes deben pertenecer al grupo correspondiente"
    const val FRACCION_YA_SALDADA = "Esta deuda ya se encuentra cancelada"
    const val PAGO_MAYOR_A_DEUDA = "No se puede cancelar un monto mayor a la deuda"
    const val PAGO_NEGATIVO = "Debe pagar un monto positivo"
    fun DESCRIPCION_LARGA(maxCaracteres: Int): String {
      return "La descripción no puede tener más de ${maxCaracteres} caracteres."
    }
    fun NOMBRE_PRODUCTO_LARGO(maxCaracteres: Int): String {
      return "Los nombres de los productos no pueden tener más de ${maxCaracteres} caracteres."
    }
}