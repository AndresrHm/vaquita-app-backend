package ar.edu.unsam.proyectosoftware.errors

object ErrorStrings {
    const val MONTO_NEGATIVO = "El monto del gasto debe ser mayor a cero."
    const val DESCRIPCION_VACIA = "La descripción no puede estar vacía."
    const val PAGADOR_NO_PERTENECE_AL_GRUPO = "El usuario pagador no pertenece al grupo seleccionado."
    const val USUARIO_SIN_PERMISO_GASTO = "El usuario no tiene permisos para registrar gastos en este grupo."
}