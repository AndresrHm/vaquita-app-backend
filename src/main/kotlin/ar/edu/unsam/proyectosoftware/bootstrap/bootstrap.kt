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
        // 1. Creamos el usuario pagador
        val usuario1 = Usuario()
        usuarioRepository.save(usuario1)

        // 3. Creamos el Grupo
        val grupo1 = Grupo(
            nombre = "Asado UNSAM",
            descripcion = "Asadito en la UNSAM!",
            imagen = "imagenDeAsadito.jpeg",
            fechaCreacion = LocalDate.of(1999, 1, 1),
        ).apply { agregarIntegrante(usuario1) }
        grupoRepository.save(grupo1)

        // 3. Creamos el Gasto inicial
        val gasto1 = Gasto(
            descripcion = "Compras para el asado",
            categoria = "COMIDA",
            monto = 15000.0,
            fecha = LocalDate.now(),
            grupo = grupo1,
            pagador = usuario1,
            participantes = mutableListOf(usuario1)
        )

        // 4. Agregamos ítems discriminados
        gasto1.agregarItem("Vacío", 2.5, 4000.0)
        gasto1.agregarItem("Carbón", 1.0, 2000.0)
        gasto1.agregarItem("Fernet", 1.0, 3000.0)

        // 5. Persistimos el Gasto
        gastoRepository.save(gasto1)
    }
}