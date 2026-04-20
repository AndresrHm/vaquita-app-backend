package ar.edu.unsam.proyectosoftware.bootstrap

import ar.edu.unsam.proyectosoftware.domain.enums.CategoriaGasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.PartesExactas
import ar.edu.unsam.proyectosoftware.domain.PartesIguales
import ar.edu.unsam.proyectosoftware.domain.RolIntegrante
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.repository.FraccionGastoRepository
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import ar.edu.unsam.proyectosoftware.service.GastoService
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate

@Configuration
class BootstrapData(
    val usuarioRepository: UsuarioRepository,
    val grupoRepository: GrupoRepository,
    val gastoRepository: GastoRepository,
    val fraccionGastoRepository: FraccionGastoRepository,
    val passwordEncoder: PasswordEncoder
) {

    @Bean
    fun initData() = CommandLineRunner {
        // 1. Usuarios
        val usuarios = listOf(
            Usuario(
                "Juan Alvarez",
                "juan.alvarez@mail.com",
                passwordEncoder.encode("contrasena123")
            ),
            Usuario(
                "Andrés Hernández",
                "andres.h@mail.com",
                passwordEncoder.encode("contrasena123")
            ),
            Usuario(
                "Agustín Hoj",
                "agustin.hoj@mail.com",
                passwordEncoder.encode("contrasena123")
            ),
            Usuario(
                "Juan Maidana",
                "juan.m@mail.com",
                passwordEncoder.encode("contrasena123")
            ),
            Usuario(
                "Giuliana Massanet",
                "giuli.m@mail.com",
                passwordEncoder.encode("contrasena123")
            ),
            Usuario(
                "Andrés Simonini",
                "andres.s@mail.com",
                passwordEncoder.encode("contrasena123")
            )
        )

        val juanA = usuarios[0]
        val andresH = usuarios[1]
        val agustinH = usuarios[2]
        val juanM = usuarios[3]
        val giulianaM = usuarios[4]
        val andresS = usuarios[5]

        juanA.apply { amigos = mutableListOf(andresH, agustinH) }
        andresH.apply { amigos = mutableListOf(agustinH, juanA) }
        agustinH.apply { amigos = mutableListOf(andresH, juanA) }

        usuarioRepository.saveAll(usuarios)

        // 2. Grupos con .addAll()
        val grupos = listOf(
            Grupo(
                nombre = "Asado UNSAM",
                descripcion = "Asadito en el campus",
                imagenUrl = "https://i.ytimg.com/vi/OWt0hpRwqSs/hqdefault.jpg",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(agustinH, juanM, andresH, giulianaM, andresS).forEach { it ->
                    agregarIntegrante(
                        it,
                        RolIntegrante.MIEMBRO
                    )
                }
                agregarIntegrante(juanA, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "Depto Palermo",
                descripcion = "Gastos de convivencia",
                imagenUrl = "https://definicion.de/wp-content/uploads/2009/02/departamento-1.jpg",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(agustinH, juanM, giulianaM).forEach { it ->
                    agregarIntegrante(
                        it,
                        RolIntegrante.MIEMBRO
                    )
                }
                agregarIntegrante(andresS, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "Los Pibes FC",
                descripcion = "Canchita los martes",
                imagenUrl = "https://www.historiadeboca.com.ar/Fotos/Equipos/2000301.jpg",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(
                    juanM, andresH, juanA
                ).forEach { it ->
                    agregarIntegrante(
                        it,
                        RolIntegrante.MIEMBRO
                    )
                }
                agregarIntegrante(giulianaM, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "Final de Objetos",
                descripcion = "Café y apuntes",
                imagenUrl = "https://www.pcgamesn.com/wp-content/sites/pcgamesn/2019/06/minecraft-enchantments-enchanting-table.jpg",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(giulianaM, juanA, andresS).forEach { it -> agregarIntegrante(it, RolIntegrante.MIEMBRO) }
                agregarIntegrante(andresS, RolIntegrante.ADMIN)
            }
        )

        grupoRepository.saveAll(grupos)

        val grupoAsado = grupos[0]
        val grupoDepto = grupos[1]
        val grupoFutbol = grupos[2]
        val grupoEstudio = grupos[3]

        // 3. Gastos
        listOf(
            PartesIguales(
                "Algo para picar",
                CategoriaGasto.COMIDA,
                60000.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.usuarios()
            ).apply { agregarItem("Picada completa", 1.0, 60000.0) },
            PartesIguales(
                "Compras para el asado",
                CategoriaGasto.COMIDA,
                15000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.usuarios(),
            ).apply {
                agregarItem("Vacío", 2.5, 4000.0); agregarItem("Carbón", 1.0, 2000.0); agregarItem(
                "Fernet",
                1.0,
                3000.0
            )
            },
            PartesIguales(
                "Bebidas previa",
                CategoriaGasto.BEBIDA,
                12000.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.usuarios(),
            ).apply { agregarItem("Cerveza", 6.0, 1500.0); agregarItem("Gaseosa", 2.0, 1500.0) },
            PartesIguales(
                "Panadería",
                CategoriaGasto.COMIDA,
                4500.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Mignon", 2.0, 1500.0); agregarItem("Facturas", 1.0, 1500.0) },
            PartesIguales(
                "Verdulería",
                CategoriaGasto.COMIDA,
                5800.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.usuarios()
            ).apply {
                agregarItem("Papa", 3.0, 1000.0); agregarItem("Tomate", 1.0, 2000.0); agregarItem(
                "Lechuga",
                1.0,
                800.0
            )
            },
            PartesIguales(
                "Más Carne",
                CategoriaGasto.COMIDA,
                18000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.usuarios()
            ).apply { agregarItem("Chorizos", 12.0, 1000.0); agregarItem("Morcilla", 6.0, 1000.0) },
            PartesIguales(
                "Hielo y Carbón extra",
                CategoriaGasto.VARIOS,
                4000.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Hielo", 3.0, 1000.0); agregarItem("Carbón", 1.0, 1000.0) },
            PartesIguales(
                "Picada",
                CategoriaGasto.COMIDA,
                9500.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Salame", 1.0, 4500.0); agregarItem("Queso", 1.0, 5000.0) },
            PartesIguales(
                "Snacks",
                CategoriaGasto.COMIDA,
                3200.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.usuarios()
            ).apply { agregarItem("Papas fritas", 2.0, 1600.0) },
            PartesIguales(
                "Postre Helado",
                CategoriaGasto.COMIDA,
                8000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.usuarios()
            ).apply { agregarItem("Bombón Suizo", 8.0, 1000.0) },
            PartesIguales(
                "Vino",
                CategoriaGasto.BEBIDA,
                11000.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Malbec", 2.0, 5500.0) },
            PartesIguales(
                "Aderezos y servilletas",
                CategoriaGasto.VARIOS,
                2500.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Mayonesa", 1.0, 1000.0); agregarItem("Servilletas", 2.0, 750.0) },
            PartesIguales(
                "Achuras",
                CategoriaGasto.COMIDA,
                7000.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.usuarios()
            ).apply { agregarItem("Chinchulines", 1.0, 4000.0); agregarItem("Riñones", 1.0, 3000.0) },
            PartesIguales(
                "Cerveza extra",
                CategoriaGasto.BEBIDA,
                9000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.usuarios()
            ).apply { agregarItem("Latas", 6.0, 1500.0) },
            PartesIguales(
                "Postre Vigilante",
                CategoriaGasto.COMIDA,
                4000.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Dulce de membrillo", 1.0, 2000.0); agregarItem("Queso fresco", 1.0, 2000.0) },
            PartesIguales(
                "Soda",
                CategoriaGasto.BEBIDA,
                2400.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Sifón", 3.0, 800.0) },
            PartesIguales(
                "Leña",
                CategoriaGasto.VARIOS,
                3000.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.usuarios()
            ).apply { agregarItem("Bolsa leña", 2.0, 1500.0) },
            PartesIguales(
                "Provoleta",
                CategoriaGasto.COMIDA,
                5000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.usuarios()
            ).apply { agregarItem("Provoleta unidad", 2.0, 2500.0) },
            PartesIguales(
                "Fernet de refuerzo",
                CategoriaGasto.BEBIDA,
                7500.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Fernet 750ml", 1.0, 7500.0) },
            PartesIguales(
                "Limones y Sal",
                CategoriaGasto.VARIOS,
                1800.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Limón", 1.0, 1000.0); agregarItem("Sal gruesa", 1.0, 800.0) },
            PartesIguales(
                "Gaseosas Dieta",
                CategoriaGasto.BEBIDA,
                4600.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.usuarios()
            ).apply { agregarItem("Pepsi Black", 2.0, 2300.0) },
            PartesIguales(
                "Carne de Cerdo",
                CategoriaGasto.COMIDA,
                12000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.usuarios()
            ).apply { agregarItem("Pechito", 1.5, 8000.0); agregarItem("Manta", 1.0, 4000.0) },
            PartesIguales(
                "Chimichurri ingredientes",
                CategoriaGasto.VARIOS,
                2200.0,
                LocalDate.now(),
                grupoAsado,
                agustinH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Aji molido", 1.0, 1100.0); agregarItem("Aceite", 1.0, 1100.0) },
            PartesIguales(
                "Vaso Descartable",
                CategoriaGasto.VARIOS,
                3500.0,
                LocalDate.now(),
                grupoAsado,
                andresH,
                grupoAsado.usuarios()
            ).apply { agregarItem("Paquete vasos", 1.0, 3500.0) },
            PartesExactas(
                "Regalo Cumpleaños",
                CategoriaGasto.VARIOS,
                30000.0,
                LocalDate.now(),
                grupoAsado,
                juanA,
                grupoAsado.usuarios(),
                mutableMapOf(juanA to 10000.0, andresH to 10000.0, agustinH to 10000.0)
            ),
            PartesExactas(
                "Bebidas Premium",
                CategoriaGasto.BEBIDA,
                20000.0,
                LocalDate.now(),
                grupoAsado,
                andresS,
                grupoAsado.usuarios(),
                mutableMapOf(andresS to 15000.0, giulianaM to 5000.0)
            ),
            PartesExactas(
                "Internet Fibra",
                CategoriaGasto.VARIOS,
                15000.0,
                LocalDate.now(),
                grupoDepto,
                andresS,
                grupoDepto.usuarios(),
                mutableMapOf(agustinH to 7500.0, juanM to 7500.0)
            ),
            PartesExactas(
                "Limpieza Profunda",
                CategoriaGasto.VARIOS,
                10000.0,
                LocalDate.now(),
                grupoDepto,
                andresS,
                grupoDepto.usuarios(),
                mutableMapOf(giulianaM to 5000.0, agustinH to 5000.0)
            ),
            PartesExactas(
                "Alquiler Cancha",
                CategoriaGasto.VARIOS,
                25000.0,
                LocalDate.now(),
                grupoFutbol,
                juanM,
                grupoFutbol.usuarios(),
                mutableMapOf(juanM to 12500.0, andresH to 12500.0)
            ),
            PartesExactas(
                "Pelota Nueva",
                CategoriaGasto.VARIOS,
                12000.0,
                LocalDate.now(),
                grupoFutbol,
                juanA,
                grupoFutbol.usuarios(),
                mutableMapOf(juanM to 4000.0, andresH to 4000.0, juanA to 4000.0)
            ),
            PartesExactas(
                "Fotocopias TP",
                CategoriaGasto.VARIOS,
                4000.0,
                LocalDate.now(),
                grupoEstudio,
                andresS,
                grupoEstudio.usuarios(),
                mutableMapOf(giulianaM to 2000.0, juanA to 2000.0)
            ),
            PartesExactas(
                "Café y medialunas",
                CategoriaGasto.COMIDA,
                6000.0,
                LocalDate.now(),
                grupoEstudio,
                giulianaM,
                grupoEstudio.usuarios(),
                mutableMapOf(giulianaM to 3000.0, andresS to 3000.0)
            )
        ).forEach { gasto ->
            val gastoGuardado = gastoRepository.save(gasto)
            val fracciones = gastoGuardado.repartirGasto()
            // Lógica de filtrado de deudas propias
            val fraccionesExternas = fracciones.filter { it.deudor.id != it.acreedor.id }
            fraccionGastoRepository.saveAll(fraccionesExternas)
        }
    }
}
