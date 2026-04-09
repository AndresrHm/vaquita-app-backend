package ar.edu.unsam.proyectosoftware.bootstrap

import ar.edu.unsam.proyectosoftware.domain.Gasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import org.springframework.boot.CommandLineRunner
import java.time.LocalDate

@Configuration
class BootstrapData(
    val usuarioRepository: UsuarioRepository,
    val grupoRepository: GrupoRepository,
    val gastoRepository: GastoRepository
) {

    @Bean
    fun initData() = CommandLineRunner {
        // 1. Usuarios
        val juanA = Usuario().apply { nombre = "Juan Alvarez" }
        val andresH = Usuario().apply { nombre = "Andrés Hernández" }
        val agustinH = Usuario().apply { nombre = "Agustín Hoj" }
        val juanM = Usuario().apply { nombre = "Juan Maidana" }
        val giulianaM = Usuario().apply { nombre = "Giuliana Massanet" }
        val andresS = Usuario().apply { nombre = "Andrés Simonini" }

        usuarioRepository.saveAll(listOf(juanA, agustinH, juanM, andresH, giulianaM, andresS))

        // 2. Grupos con .addAll()
        val grupoAsado = Grupo(
            nombre = "Asado UNSAM",
            descripcion = "Asadito en el campus",
            imagenUrl = "asado.jpg",
            fechaCreacion = LocalDate.now()
        ).apply { integrantes.addAll(listOf(juanA, agustinH, andresH, andresS)) }

        val grupoDepto = Grupo(
            nombre = "Depto Palermo",
            descripcion = "Gastos de convivencia",
            imagenUrl = "depto.jpg",
            fechaCreacion = LocalDate.now()
        ).apply { integrantes.addAll(listOf(agustinH, juanM, giulianaM)) }

        val grupoFutbol = Grupo(
            nombre = "Los Pibes FC",
            descripcion = "Canchita los martes",
            imagenUrl = "pelota.jpg",
            fechaCreacion = LocalDate.now()
        ).apply { integrantes.addAll(listOf(juanM, andresH, juanA)) }

        val grupoEstudio = Grupo(
            nombre = "Final de Objetos",
            descripcion = "Café y apuntes",
            imagenUrl = "estudio.jpg",
            fechaCreacion = LocalDate.now()
        ).apply { integrantes.addAll(listOf(giulianaM, juanA, agustinH, andresS)) }

        grupoRepository.saveAll(listOf(grupoAsado, grupoDepto, grupoFutbol, grupoEstudio))

        // 3. Un gasto de prueba en el Grupo 1
        val gasto1 = Gasto(
            descripcion = "Compras para el asado",
            categoria = "COMIDA",
            monto = 15000.0,
            fecha = LocalDate.now(),
            grupo = grupoAsado,
            pagador = juanA,
            // Usamos una copia de la lista para no marear a Hibernate
            participantes = grupoAsado.integrantes.toMutableList()
        )

        gasto1.agregarItem("Vacío", 2.5, 4000.0)
        gasto1.agregarItem("Carbón", 1.0, 2000.0)
        gasto1.agregarItem("Fernet", 1.0, 3000.0)

        gastoRepository.save(gasto1)
    }
}