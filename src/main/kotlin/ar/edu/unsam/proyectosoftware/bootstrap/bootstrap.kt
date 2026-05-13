package ar.edu.unsam.proyectosoftware.bootstrap

import ar.edu.unsam.proyectosoftware.domain.Aleatorio
import ar.edu.unsam.proyectosoftware.domain.enums.CategoriaGasto
import ar.edu.unsam.proyectosoftware.domain.Grupo
import ar.edu.unsam.proyectosoftware.domain.PartesExactas
import ar.edu.unsam.proyectosoftware.domain.PartesIguales
import ar.edu.unsam.proyectosoftware.domain.Porcentual
import ar.edu.unsam.proyectosoftware.domain.RolIntegrante
import ar.edu.unsam.proyectosoftware.domain.Usuario
import ar.edu.unsam.proyectosoftware.dto.TipoActividad
import ar.edu.unsam.proyectosoftware.repository.FraccionGastoRepository
import ar.edu.unsam.proyectosoftware.repository.GastoRepository
import ar.edu.unsam.proyectosoftware.repository.GrupoRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ar.edu.unsam.proyectosoftware.repository.UsuarioRepository
import ar.edu.unsam.proyectosoftware.service.ActividadService
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate

@Configuration
class BootstrapData(
    val usuarioRepository: UsuarioRepository,
    val grupoRepository: GrupoRepository,
    val gastoRepository: GastoRepository,
    val fraccionGastoRepository: FraccionGastoRepository,
    val passwordEncoder: PasswordEncoder,
    val actividadService: ActividadService,
) {

    @Bean
    fun initData() = CommandLineRunner {

        // 1. Usuarios
        val pass = passwordEncoder.encode("contrasena123")
        val usuarios = listOf(
            Usuario("Juan Alvarez", "juan.alvarez@mail.com", pass, "https://i.pravatar.cc/150?img=12", "juan.alvarez"),
            Usuario("Andrés Hernández", "andres.h@mail.com", pass, "https://i.pravatar.cc/150?img=15", "andres.h"),
            Usuario("Agustín Hoj", "agustin.hoj@mail.com", pass, "https://i.pravatar.cc/150?img=18", "agustin.hoj"),
            Usuario("Juan Maidana", "juan.m@mail.com", pass, "https://i.pravatar.cc/150?img=21", "juan.maidana"),
            Usuario("Giuliana Massanet", "giuli.m@mail.com", pass, "https://i.pravatar.cc/150?img=47", "giuli.m"),
            Usuario("Andrés Simonini", "andres.s@mail.com", pass, "https://i.pravatar.cc/150?img=33", "andres.s"),
            Usuario("Camila Rojas", "camila.rojas@mail.com", pass, "https://i.pravatar.cc/150?img=44", "camila.rojas"),
            Usuario("Lucas Pereyra", "lucas.p@mail.com", pass, "https://i.pravatar.cc/150?img=56", "lucas.p"),
            Usuario("Valentina Nuñez", "valentina.n@mail.com", pass, "https://i.pravatar.cc/150?img=49", "valentina.n"),
            Usuario("Matías Ferreyra", "matias.f@mail.com", pass, "https://i.pravatar.cc/150?img=60", "matias.f"),
            Usuario("Sofía López", "sofia.l@mail.com", pass, "https://i.pravatar.cc/150?img=53", "sofia.l"),
        )

        val juanA = usuarios[0]
        val andresH = usuarios[1]
        val agustinH = usuarios[2]
        val juanM = usuarios[3]
        val giulianaM = usuarios[4]
        val andresS = usuarios[5]
        val camilaR = usuarios[6]
        val lucasP = usuarios[7]
        val valentinaN = usuarios[8]
        val matiasF = usuarios[9]
        val sofiaL = usuarios[10]

        juanA.apply { amigos = mutableListOf(andresH, agustinH) }
        andresH.apply { amigos = mutableListOf(agustinH, juanA, camilaR, matiasF) }
        agustinH.apply { amigos = mutableListOf(andresH, juanA, giulianaM, valentinaN) }
        juanM.apply { amigos = mutableListOf(giulianaM, matiasF, lucasP) }
        giulianaM.apply { amigos = mutableListOf(juanM, agustinH, valentinaN, camilaR) }
        andresS.apply { amigos = mutableListOf(sofiaL, camilaR, juanA) }
        camilaR.apply { amigos = mutableListOf(andresS, andresH, giulianaM) }
        lucasP.apply { amigos = mutableListOf(juanA, juanM, matiasF) }
        valentinaN.apply { amigos = mutableListOf(agustinH, giulianaM, sofiaL) }
        matiasF.apply { amigos = mutableListOf(juanM, lucasP, andresH) }
        sofiaL.apply { amigos = mutableListOf(andresS, valentinaN, juanA) }

        usuarioRepository.saveAll(usuarios)

        val todosLosUsuarios = usuarios

        // Registrar actividades de creación de cuenta
        todosLosUsuarios.forEach { usuario ->
            actividadService.registrarActividad(
                usuario = usuario,
                tipo = TipoActividad.CUENTA_CREADA,
                descripcion = "Tu cuenta fue creada exitosamente.",
                metadata = mapOf(
                    "usuarioId" to usuario.id,
                    "email" to usuario.email,
                )
            )
        }

        // 2. Grupos
        val grupos = listOf(
            Grupo(
                nombre = "Asado UNSAM",
                descripcion = "Asadito en el campus",
                imagenUrl = "https://i.ytimg.com/vi/OWt0hpRwqSs/hqdefault.jpg",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(agustinH, juanM, andresH, giulianaM, andresS).forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(juanA, RolIntegrante.ADMIN)
                modoRata = true
            },
            Grupo(
                nombre = "Depto Palermo",
                descripcion = "Gastos de convivencia",
                imagenUrl = "https://definicion.de/wp-content/uploads/2009/02/departamento-1.jpg",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(agustinH, juanM, giulianaM).forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(andresS, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "Los Pibes FC",
                descripcion = "Canchita los martes",
                imagenUrl = "https://www.historiadeboca.com.ar/Fotos/Equipos/2000301.jpg",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(juanM, andresH, juanA).forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(giulianaM, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "Final de Objetos",
                descripcion = "Café y apuntes",
                imagenUrl = "https://www.pcgamesn.com/wp-content/sites/pcgamesn/2019/06/minecraft-enchantments-enchanting-table.jpg",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(giulianaM, juanA).forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(andresS, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "Escapada Córdoba",
                descripcion = "Organización de combustible y peajes",
                imagenUrl = "https://media.istockphoto.com/id/1365317354/photo/group-of-friends-on-road-trip.jpg",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(juanA, andresS, valentinaN, sofiaL).forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(camilaR, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "Cumple de Matu",
                descripcion = "Decoración, comida y bebidas para el cumple",
                imagenUrl = "https://cdn0.bodas.com.mx/article-vendor/2267/3_2/1280/jpg/portada-2_5_112267-166458303624924.jpeg",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(juanM, matiasF, agustinH, giulianaM).forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(lucasP, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "Team 7AM",
                descripcion = "Suplementos y clases grupales",
                imagenUrl = "https://www.mensjournal.com/.image/t_share/MTk2MTM2NjA3NzQ5MzQxMzk3/men-working-out-in-gym.jpg",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(andresH, juanA, sofiaL, matiasF).forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(valentinaN, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "HackLab Nocturno",
                descripcion = "Juntadas para codear proyectos y aprender",
                imagenUrl = "https://images.unsplash.com/photo-1515879218367-8466d910aaa4",
                fechaCreacion = LocalDate.now()
            ).apply {
                todosLosUsuarios.filter { it != juanA }.forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(juanA, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "Maratón de Series",
                descripcion = "Suscripciones y picada para maratones del finde",
                imagenUrl = "https://images.unsplash.com/photo-1593784991095-a205069470b6",
                fechaCreacion = LocalDate.now()
            ).apply {
                todosLosUsuarios.filter { it != camilaR }.forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(camilaR, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "Plan de Finde",
                descripcion = "Salidas, transporte y gastos compartidos",
                imagenUrl = "https://images.unsplash.com/photo-1529156069898-49953e39b3ac",
                fechaCreacion = LocalDate.now()
            ).apply {
                todosLosUsuarios.filter { it != valentinaN }.forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(valentinaN, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "Soy una bestia",
                descripcion = "Club de fans de Miguel Merentiel",
                imagenUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSUZWAPD0571F8H7JjkCcwcvQg6A8AKlFpnbxm1NZ674nX610rRGuX7P00-If0i_Fy-VOup5HtqV88JQieYFc4u6N490NigP-rBI-OKH9Qw&s=10",
                fechaCreacion = LocalDate.now()
            ).apply {
                agregarIntegrante(juanA, RolIntegrante.ADMIN)
                agregarIntegrante(andresH, RolIntegrante.MIEMBRO)
            },
            Grupo(
                nombre = "Merentielistas de verdad",
                descripcion = "Merentiel está gagá",
                imagenUrl = "https://pbs.twimg.com/media/HGS_zikaQAA2NdL?format=jpg&name=small",
                fechaCreacion = LocalDate.now()
            ).apply {
                agregarIntegrante(juanA, RolIntegrante.ADMIN)
                agregarIntegrante(lucasP, RolIntegrante.MIEMBRO)
                agregarIntegrante(matiasF, RolIntegrante.MIEMBRO)
            },
            Grupo(
                nombre = "Salida al casino",
                descripcion = "Lo que pasa en el casino, queda en el casino",
                imagenUrl = "https://images.unsplash.com/photo-1596838132731-3301c3fd4317",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(juanA, andresH, agustinH, juanM, giulianaM).forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(andresS, RolIntegrante.ADMIN)
            },
        )

        val gruposFiltro = listOf(
            Grupo(
                nombre = "Filtro - Todos sin gastos",
                descripcion = "Grupo de control para verificar el filtro all",
                imagenUrl = "https://images.unsplash.com/photo-1529156069898-49953e39b3ac",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(juanA, andresH, agustinH).forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(giulianaM, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "Filtro - Juan debe",
                descripcion = "Juan tiene saldo pendiente a favor de otros",
                imagenUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(juanA, andresH, agustinH).forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(andresS, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "Filtro - A Juan le deben",
                descripcion = "Otros usuarios le deben dinero a Juan",
                imagenUrl = "https://images.unsplash.com/photo-1497366754035-f200968a6e72",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(juanA, andresH, agustinH).forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(camilaR, RolIntegrante.ADMIN)
            },
            Grupo(
                nombre = "Filtro - Pendiente mixto",
                descripcion = "Grupo con deudas en ambos sentidos para probar pending",
                imagenUrl = "https://images.unsplash.com/photo-1515169067868-5387ec356754",
                fechaCreacion = LocalDate.now()
            ).apply {
                listOf(juanA, andresH, agustinH).forEach {
                    agregarIntegrante(it, RolIntegrante.MIEMBRO)
                }
                agregarIntegrante(lucasP, RolIntegrante.ADMIN)
            },
        )

        grupoRepository.saveAll(grupos + gruposFiltro)

        val grupoAsado = grupos[0]
        val grupoDepto = grupos[1]
        val grupoFutbol = grupos[2]
        val grupoEstudio = grupos[3]
        val grupoViaje = grupos[4]
        val grupoCumple = grupos[5]
        val grupoGym = grupos[6]
        val grupoHackLab = grupos[7]
        val grupoSeries = grupos[8]
        val grupoFinde = grupos[9]
        val grupoBestia = grupos[10]
        val grupoAntiBestia = grupos[11]
        val grupoCasino = grupos[12]
        val grupoFiltroTodos = gruposFiltro[0]
        val grupoFiltroJuanDebe = gruposFiltro[1]
        val grupoFiltroJuanLeDeben = gruposFiltro[2]
        val grupoFiltroPendienteMixto = gruposFiltro[3]

        // Registrar actividades de ingreso a grupos
        listOf(
            grupoAsado to listOf(agustinH, juanM, andresH, giulianaM, andresS, juanA),
            grupoDepto to listOf(agustinH, juanM, giulianaM, andresS),
            grupoFutbol to listOf(juanM, andresH, juanA, giulianaM),
            grupoEstudio to listOf(giulianaM, juanA, andresS),
            grupoViaje to listOf(juanA, andresS, valentinaN, sofiaL, camilaR),
            grupoCumple to listOf(juanM, matiasF, agustinH, giulianaM, lucasP),
            grupoGym to listOf(andresH, juanA, sofiaL, matiasF, valentinaN),
            grupoHackLab to todosLosUsuarios,
            grupoSeries to todosLosUsuarios.filter { it != camilaR }.plus(camilaR),
            grupoBestia to listOf(juanA, andresH),
            grupoAntiBestia to listOf(juanA, lucasP, matiasF),
            grupoCasino to listOf(juanA, andresH, agustinH, juanM, giulianaM, andresS),
            grupoFinde to todosLosUsuarios.filter { it != valentinaN }.plus(valentinaN),
            grupoFiltroTodos to listOf(juanA, andresH, agustinH, giulianaM),
            grupoFiltroJuanDebe to listOf(juanA, andresH, agustinH),
            grupoFiltroJuanLeDeben to listOf(juanA, andresH, agustinH),
            grupoFiltroPendienteMixto to listOf(juanA, andresH, agustinH),
        ).forEach { (grupo, miembros) ->
            miembros.forEach { usuario ->
                actividadService.registrarActividad(
                    usuario = usuario,
                    tipo = TipoActividad.INGRESO_GRUPO,
                    descripcion = "Te uniste al grupo ${grupo.nombre}.",
                    metadata = mapOf(
                        "grupoId" to grupo.id,
                        "grupoNombre" to grupo.nombre,
                    )
                )
            }
        }

        // 3. Gastos
        var diasAtras = 0L
        fun nextFecha(): LocalDate = LocalDate.now().minusDays(diasAtras++)

        listOf(
            // ── Asado UNSAM ──────────────────────────────────────────────
            PartesIguales(
                "Algo para picar", CategoriaGasto.COMIDA, 60000.0, nextFecha(),
                grupoAsado, andresS, grupoAsado.usuarios()
            ).apply { agregarItem("Picada completa", 1.0, 60000.0) },

            PartesIguales(
                "Compras para el asado", CategoriaGasto.COMIDA, 15000.0, nextFecha(),
                grupoAsado, juanA, grupoAsado.usuarios()
            ).apply {
                agregarItem("Vacío", 2.5, 4000.0)
                agregarItem("Carbón", 1.0, 2000.0)
                agregarItem("Fernet", 1.0, 3000.0)
            },

            PartesIguales(
                "Bebidas previa", CategoriaGasto.BEBIDA, 12000.0, nextFecha(),
                grupoAsado, agustinH, grupoAsado.usuarios()
            ).apply {
                agregarItem("Cerveza", 6.0, 1500.0)
                agregarItem("Gaseosa", 2.0, 1500.0)
            },

            PartesIguales(
                "Panadería", CategoriaGasto.COMIDA, 4500.0, nextFecha(),
                grupoAsado, andresH, grupoAsado.usuarios()
            ).apply {
                agregarItem("Mignon", 2.0, 1500.0)
                agregarItem("Facturas", 1.0, 1500.0)
            },

            PartesIguales(
                "Verdulería", CategoriaGasto.COMIDA, 5800.0, nextFecha(),
                grupoAsado, andresS, grupoAsado.usuarios()
            ).apply {
                agregarItem("Papa", 3.0, 1000.0)
                agregarItem("Tomate", 1.0, 2000.0)
                agregarItem("Lechuga", 1.0, 800.0)
            },

            PartesIguales(
                "Más Carne", CategoriaGasto.COMIDA, 18000.0, nextFecha(),
                grupoAsado, juanA, grupoAsado.usuarios()
            ).apply {
                agregarItem("Chorizos", 12.0, 1000.0)
                agregarItem("Morcilla", 6.0, 1000.0)
            },

            PartesIguales(
                "Hielo y Carbón extra", CategoriaGasto.VARIOS, 4000.0, nextFecha(),
                grupoAsado, agustinH, grupoAsado.usuarios()
            ).apply {
                agregarItem("Hielo", 3.0, 1000.0)
                agregarItem("Carbón", 1.0, 1000.0)
            },

            PartesIguales(
                "Picada", CategoriaGasto.COMIDA, 9500.0, nextFecha(),
                grupoAsado, andresH, grupoAsado.usuarios()
            ).apply {
                agregarItem("Salame", 1.0, 4500.0)
                agregarItem("Queso", 1.0, 5000.0)
            },

            PartesIguales(
                "Snacks", CategoriaGasto.COMIDA, 3200.0, nextFecha(),
                grupoAsado, andresS, grupoAsado.usuarios()
            ).apply { agregarItem("Papas fritas", 2.0, 1600.0) },

            PartesIguales(
                "Postre Helado", CategoriaGasto.COMIDA, 8000.0, nextFecha(),
                grupoAsado, juanA, grupoAsado.usuarios()
            ).apply { agregarItem("Bombón Suizo", 8.0, 1000.0) },

            PartesIguales(
                "Vino", CategoriaGasto.BEBIDA, 11000.0, nextFecha(),
                grupoAsado, agustinH, grupoAsado.usuarios()
            ).apply { agregarItem("Malbec", 2.0, 5500.0) },

            PartesIguales(
                "Aderezos y servilletas", CategoriaGasto.VARIOS, 2500.0, nextFecha(),
                grupoAsado, andresH, grupoAsado.usuarios()
            ).apply {
                agregarItem("Mayonesa", 1.0, 1000.0)
                agregarItem("Servilletas", 2.0, 750.0)
            },

            PartesIguales(
                "Achuras", CategoriaGasto.COMIDA, 7000.0, nextFecha(),
                grupoAsado, andresS, grupoAsado.usuarios()
            ).apply {
                agregarItem("Chinchulines", 1.0, 4000.0)
                agregarItem("Riñones", 1.0, 3000.0)
            },

            PartesIguales(
                "Cerveza extra", CategoriaGasto.BEBIDA, 9000.0, nextFecha(),
                grupoAsado, juanA, grupoAsado.usuarios()
            ).apply { agregarItem("Latas", 6.0, 1500.0) },

            PartesIguales(
                "Postre Vigilante", CategoriaGasto.COMIDA, 4000.0, nextFecha(),
                grupoAsado, agustinH, grupoAsado.usuarios()
            ).apply {
                agregarItem("Dulce de membrillo", 1.0, 2000.0)
                agregarItem("Queso fresco", 1.0, 2000.0)
            },

            PartesIguales(
                "Soda", CategoriaGasto.BEBIDA, 2400.0, nextFecha(),
                grupoAsado, andresH, grupoAsado.usuarios()
            ).apply { agregarItem("Sifón", 3.0, 800.0) },

            PartesIguales(
                "Leña", CategoriaGasto.VARIOS, 3000.0, nextFecha(),
                grupoAsado, andresS, grupoAsado.usuarios()
            ).apply { agregarItem("Bolsa leña", 2.0, 1500.0) },

            PartesIguales(
                "Provoleta", CategoriaGasto.COMIDA, 5000.0, nextFecha(),
                grupoAsado, juanA, grupoAsado.usuarios()
            ).apply { agregarItem("Provoleta unidad", 2.0, 2500.0) },

            PartesIguales(
                "Fernet de refuerzo", CategoriaGasto.BEBIDA, 7500.0, nextFecha(),
                grupoAsado, agustinH, grupoAsado.usuarios()
            ).apply { agregarItem("Fernet 750ml", 1.0, 7500.0) },

            PartesIguales(
                "Limones y Sal", CategoriaGasto.VARIOS, 1800.0, nextFecha(),
                grupoAsado, andresH, grupoAsado.usuarios()
            ).apply {
                agregarItem("Limón", 1.0, 1000.0)
                agregarItem("Sal gruesa", 1.0, 800.0)
            },

            PartesIguales(
                "Gaseosas Dieta", CategoriaGasto.BEBIDA, 4600.0, nextFecha(),
                grupoAsado, andresS, grupoAsado.usuarios()
            ).apply { agregarItem("Pepsi Black", 2.0, 2300.0) },

            PartesIguales(
                "Carne de Cerdo", CategoriaGasto.COMIDA, 12000.0, nextFecha(),
                grupoAsado, juanA, grupoAsado.usuarios()
            ).apply {
                agregarItem("Pechito", 1.5, 8000.0)
                agregarItem("Manta", 1.0, 4000.0)
            },

            PartesIguales(
                "Chimichurri ingredientes", CategoriaGasto.VARIOS, 2200.0, nextFecha(),
                grupoAsado, agustinH, grupoAsado.usuarios()
            ).apply {
                agregarItem("Aji molido", 1.0, 1100.0)
                agregarItem("Aceite", 1.0, 1100.0)
            },

            PartesIguales(
                "Vaso Descartable", CategoriaGasto.VARIOS, 3500.0, nextFecha(),
                grupoAsado, andresH, grupoAsado.usuarios()
            ).apply { agregarItem("Paquete vasos", 1.0, 3500.0) },

            PartesExactas(
                "Regalo Cumpleaños", CategoriaGasto.VARIOS, 35000.0, nextFecha(),
                grupoAsado, juanA, grupoAsado.usuarios(),
                mutableMapOf(juanA to 20000.0, andresH to 10000.0, agustinH to 5000.0)
            ),

            PartesExactas(
                "Bebidas Premium", CategoriaGasto.BEBIDA, 25000.0, nextFecha(),
                grupoAsado, andresS, grupoAsado.usuarios(),
                mutableMapOf(andresS to 20000.0, giulianaM to 5000.0)
            ),

            // ── Depto Palermo ─────────────────────────────────────────────
            PartesIguales(
                "Compra supermercado", CategoriaGasto.COMIDA, 32600.0, nextFecha(),
                grupoDepto, andresS, grupoDepto.usuarios()
            ).apply {
                agregarItem("Limpieza", 1.0, 10600.0)
                agregarItem("Comida semanal", 1.0, 22000.0)
            },

            PartesIguales(
                "Internet y cable", CategoriaGasto.HOGAR, 18000.0, nextFecha(),
                grupoDepto, giulianaM, grupoDepto.usuarios()
            ).apply { agregarItem("Abono mensual", 1.0, 18000.0) },

            PartesIguales(
                "Gas envasado", CategoriaGasto.HOGAR, 9600.0, nextFecha(),
                grupoDepto, juanM, grupoDepto.usuarios()
            ).apply { agregarItem("Recarga garrafa", 2.0, 4800.0) },

            PartesExactas(
                "Internet Fibra", CategoriaGasto.HOGAR, 12000.0, nextFecha(),
                grupoDepto, andresS, grupoDepto.usuarios(),
                mutableMapOf(agustinH to 7500.0, juanM to 4500.0)
            ),

            PartesExactas(
                "Limpieza Profunda", CategoriaGasto.VARIOS, 12000.0, nextFecha(),
                grupoDepto, andresS, grupoDepto.usuarios(),
                mutableMapOf(giulianaM to 4000.0, agustinH to 8000.0)
            ),

            // ── Los Pibes FC ──────────────────────────────────────────────
            PartesIguales(
                "Reserva de cancha", CategoriaGasto.VARIOS, 24000.0, nextFecha(),
                grupoFutbol, giulianaM, grupoFutbol.usuarios()
            ).apply { agregarItem("Alquiler cancha", 1.0, 24000.0) },

            PartesIguales(
                "Pelota nueva", CategoriaGasto.VARIOS, 18500.0, nextFecha(),
                grupoFutbol, andresH, grupoFutbol.usuarios()
            ).apply { agregarItem("Pelota N5", 1.0, 18500.0) },

            PartesIguales(
                "Tercer tiempo", CategoriaGasto.BEBIDA, 14800.0, nextFecha(),
                grupoFutbol, juanM, grupoFutbol.usuarios()
            ).apply {
                agregarItem("Agua", 6.0, 800.0)
                agregarItem("Isotónica", 8.0, 1250.0)
            },

            PartesExactas(
                "Alquiler Cancha", CategoriaGasto.VARIOS, 25000.0, nextFecha(),
                grupoFutbol, juanM, grupoFutbol.usuarios(),
                mutableMapOf(juanM to 12000.0, andresH to 13000.0)
            ),

            PartesExactas(
                "Pelota Nueva", CategoriaGasto.VARIOS, 12000.0, nextFecha(),
                grupoFutbol, juanA, grupoFutbol.usuarios(),
                mutableMapOf(juanM to 5000.0, andresH to 3000.0, juanA to 4000.0)
            ),

            // ── Final de Objetos ──────────────────────────────────────────
            PartesIguales(
                "Fotocopias final", CategoriaGasto.OTROS, 7200.0, nextFecha(),
                grupoEstudio, giulianaM, grupoEstudio.usuarios()
            ).apply { agregarItem("Apuntes", 3.0, 2400.0) },

            PartesIguales(
                "Cafe de estudio", CategoriaGasto.COMIDA, 5600.0, nextFecha(),
                grupoEstudio, juanA, grupoEstudio.usuarios()
            ).apply { agregarItem("Cafe doble", 4.0, 1400.0) },

            PartesExactas(
                "Fotocopias TP", CategoriaGasto.OTROS, 5000.0, nextFecha(),
                grupoEstudio, andresS, grupoEstudio.usuarios(),
                mutableMapOf(giulianaM to 3000.0, juanA to 2000.0)
            ),

            PartesExactas(
                "Café y medialunas", CategoriaGasto.COMIDA, 6000.0, nextFecha(),
                grupoEstudio, giulianaM, grupoEstudio.usuarios(),
                mutableMapOf(giulianaM to 3000.0, andresS to 3000.0)
            ),

            // ── Escapada Córdoba ──────────────────────────────────────────
            PartesIguales(
                "Combustible", CategoriaGasto.TRANSPORTE, 68000.0, nextFecha(),
                grupoViaje, camilaR, grupoViaje.usuarios()
            ).apply { agregarItem("Nafta super", 40.0, 1700.0) },

            PartesIguales(
                "Peajes", CategoriaGasto.TRANSPORTE, 21500.0, nextFecha(),
                grupoViaje, andresS, grupoViaje.usuarios()
            ).apply { agregarItem("Cabinas", 5.0, 4300.0) },

            PartesIguales(
                "Snacks ruta", CategoriaGasto.COMIDA, 12400.0, nextFecha(),
                grupoViaje, sofiaL, grupoViaje.usuarios()
            ).apply {
                agregarItem("Galletitas", 4.0, 1100.0)
                agregarItem("Agua", 8.0, 1000.0)
            },

            // ── Cumple de Matu ────────────────────────────────────────────
            PartesIguales(
                "Decoracion", CategoriaGasto.VARIOS, 29500.0, nextFecha(),
                grupoCumple, lucasP, grupoCumple.usuarios()
            ).apply {
                agregarItem("Globos", 5.0, 1900.0)
                agregarItem("Banderines", 3.0, 6700.0)
            },

            PartesIguales(
                "Torta y velas", CategoriaGasto.COMIDA, 36800.0, nextFecha(),
                grupoCumple, agustinH, grupoCumple.usuarios()
            ).apply {
                agregarItem("Torta", 1.0, 34000.0)
                agregarItem("Velas", 2.0, 1400.0)
            },

            PartesIguales(
                "Bebidas", CategoriaGasto.BEBIDA, 22600.0, nextFecha(),
                grupoCumple, giulianaM, grupoCumple.usuarios()
            ).apply {
                agregarItem("Gaseosa", 8.0, 1600.0)
                agregarItem("Jugo", 6.0, 1600.0)
            },

            // ── Team 7AM ──────────────────────────────────────────────────
            PartesIguales(
                "Cuota mensual", CategoriaGasto.OTROS, 90000.0, nextFecha(),
                grupoGym, valentinaN, grupoGym.usuarios()
            ).apply { agregarItem("Pase grupal", 5.0, 18000.0) },

            PartesIguales(
                "Proteinas", CategoriaGasto.OTROS, 45500.0, nextFecha(),
                grupoGym, matiasF, grupoGym.usuarios()
            ).apply {
                agregarItem("Whey", 2.0, 21000.0)
                agregarItem("Creatina", 1.0, 3500.0)
            },

            PartesIguales(
                "Clase funcional", CategoriaGasto.OTROS, 28000.0, nextFecha(),
                grupoGym, sofiaL, grupoGym.usuarios()
            ).apply { agregarItem("Pack 10 clases", 1.0, 28000.0) },

            // ── HackLab Nocturno ──────────────────────────────────────────
            PartesIguales(
                "Suscripcion herramientas", CategoriaGasto.OTROS, 36000.0, nextFecha(),
                grupoHackLab, juanA, grupoHackLab.usuarios()
            ).apply {
                agregarItem("GitHub Team", 1.0, 22000.0)
                agregarItem("Figma", 1.0, 14000.0)
            },

            PartesIguales(
                "Pizzas de deploy", CategoriaGasto.COMIDA, 27500.0, nextFecha(),
                grupoHackLab, camilaR, grupoHackLab.usuarios()
            ).apply {
                agregarItem("Muzzarella", 3.0, 6500.0)
                agregarItem("Fugazzeta", 1.0, 8000.0)
            },

            PartesIguales(
                "Cafes y energia", CategoriaGasto.BEBIDA, 12800.0, nextFecha(),
                grupoHackLab, andresH, grupoHackLab.usuarios()
            ).apply {
                agregarItem("Cafe", 8.0, 1000.0)
                agregarItem("Energizante", 3.0, 1600.0)
            },

            // ── Maratón de Series ─────────────────────────────────────────
            PartesIguales(
                "Suscripciones streaming", CategoriaGasto.ENTRETENIMIENTO, 24900.0, nextFecha(),
                grupoSeries, camilaR, grupoSeries.usuarios()
            ).apply { agregarItem("Pack plataformas", 1.0, 24900.0) },

            PartesIguales(
                "Picada maraton", CategoriaGasto.COMIDA, 19800.0, nextFecha(),
                grupoSeries, giulianaM, grupoSeries.usuarios()
            ).apply {
                agregarItem("Snacks", 6.0, 1800.0)
                agregarItem("Sandwiches", 3.0, 3000.0)
            },

            PartesIguales(
                "Gaseosas y jugos", CategoriaGasto.BEBIDA, 14400.0, nextFecha(),
                grupoSeries, valentinaN, grupoSeries.usuarios()
            ).apply {
                agregarItem("Gaseosa 2.25", 6.0, 1700.0)
                agregarItem("Jugo", 3.0, 1400.0)
            },

            // ── Plan de Finde ─────────────────────────────────────────────
            PartesIguales(
                "Nafta salida del sabado", CategoriaGasto.TRANSPORTE, 45200.0, nextFecha(),
                grupoFinde, valentinaN, grupoFinde.usuarios()
            ).apply { agregarItem("Carga YPF", 1.0, 45200.0) },

            PartesIguales(
                "Asado del domingo", CategoriaGasto.COMIDA, 53800.0, nextFecha(),
                grupoFinde, juanM, grupoFinde.usuarios()
            ).apply {
                agregarItem("Carne", 1.0, 37000.0)
                agregarItem("Carbon", 2.0, 4200.0)
                agregarItem("Ensaladas", 1.0, 8400.0)
            },

            PartesIguales(
                "Entradas recital", CategoriaGasto.ENTRETENIMIENTO, 66000.0, nextFecha(),
                grupoFinde, lucasP, grupoFinde.usuarios()
            ).apply { agregarItem("Tickets campo", 4.0, 16500.0) },

            // ── Merentiel ─────────────────────────────────────────────
            PartesIguales(
                "Gorras de Merentiel", CategoriaGasto.VARIOS, 10000.0, nextFecha(),
                grupoBestia, andresH, grupoBestia.usuarios()
            ),

            PartesIguales(
                "Torta de cumpleaños", CategoriaGasto.COMIDA, 5000.0, nextFecha(),
                grupoBestia, juanA, grupoBestia.usuarios()
            ),

            // ── Merentieln't ─────────────────────────────────────────────
            Porcentual(
                "Torta de velorio", CategoriaGasto.COMIDA, 10000.0, nextFecha(),
                grupoAntiBestia, juanA, grupoBestia.usuarios(),
                mutableMapOf(juanA to 10.0, lucasP to 50.0, matiasF to 40.0)
            ),

            Porcentual(
                "Gorras de luto", CategoriaGasto.VARIOS, 6000.0, nextFecha(),
                grupoAntiBestia, juanA, grupoBestia.usuarios(),
                mutableMapOf(juanA to 60.0, lucasP to 20.0, matiasF to 20.0)
            ),

            Aleatorio(
                "$250.000 en NFT", CategoriaGasto.ENTRETENIMIENTO, 250000.0, nextFecha(),
                grupoAntiBestia, lucasP, grupoBestia.usuarios()
            ),

            // ── Casino ─────────────────────────────────────────────
            Aleatorio(
                "Fichas ruleta", CategoriaGasto.ENTRETENIMIENTO, 45000.0, nextFecha(),
                grupoCasino, juanA, grupoCasino.usuarios()
            ),

            Aleatorio(
                "Tragamonedas ronda 1", CategoriaGasto.ENTRETENIMIENTO, 28000.0, nextFecha(),
                grupoCasino, andresH, grupoCasino.usuarios()
            ),

            Aleatorio(
                "Bebidas en el piso", CategoriaGasto.BEBIDA, 28500.0, nextFecha(),
                grupoCasino, agustinH, grupoCasino.usuarios()
            ),

            Aleatorio(
                "Blackjack mesa VIP", CategoriaGasto.ENTRETENIMIENTO, 120000.0, nextFecha(),
                grupoCasino, juanM, grupoCasino.usuarios()
            ),

            Aleatorio(
                "Cena post casino", CategoriaGasto.COMIDA, 67000.0, nextFecha(),
                grupoCasino, giulianaM, grupoCasino.usuarios()
            ),


        ).forEach { gasto ->
            val gastoGuardado = gastoRepository.save(gasto)
            val fracciones = gastoGuardado.repartirGasto()
            val fraccionesExternas = fracciones.filter { it.deudor.id != it.acreedor.id }
            fraccionGastoRepository.saveAll(fraccionesExternas)

            // Registrar actividades de deuda
            // Actividad de deuda generada para el pagador
            actividadService.registrarActividad(
                usuario = gastoGuardado.pagador,
                tipo = TipoActividad.DEUDA_GENERADA,
                descripcion = "Pagaste ${gastoGuardado.descripcion} en ${gastoGuardado.grupo.nombre}",
                metadata = mapOf(
                    "gastoId" to gastoGuardado.id,
                    "descripcion" to gastoGuardado.descripcion,
                    "monto" to gastoGuardado.monto,
                    "grupoId" to gastoGuardado.grupo.id,
                    "grupoNombre" to gastoGuardado.grupo.nombre,
                )
            )

            // Actividades de gasto deuda para cada deudor
            fraccionesExternas.forEach { fraccion ->
                actividadService.registrarActividad(
                    usuario = fraccion.deudor,
                    tipo = TipoActividad.GASTO_DEUDA,
                    descripcion = "Debes pagar ${fraccion.montoFraccion} a ${fraccion.acreedor.nombre} por ${gastoGuardado.descripcion}",
                    metadata = mapOf(
                        "gastoId" to gastoGuardado.id,
                        "descripcion" to gastoGuardado.descripcion,
                        "monto" to fraccion.montoFraccion,
                        "acreedor" to fraccion.acreedor.nombre,
                        "grupoId" to gastoGuardado.grupo.id,
                        "grupoNombre" to gastoGuardado.grupo.nombre,
                    )
                )
            }
        }

        listOf(
            PartesIguales(
                "Filtro - Juan debe", CategoriaGasto.COMIDA, 12000.0, nextFecha(),
                grupoFiltroJuanDebe, andresH, grupoFiltroJuanDebe.usuarios()
            ).apply { agregarItem("Cena compartida", 1.0, 12000.0) },

            PartesIguales(
                "Filtro - A Juan le deben", CategoriaGasto.VARIOS, 18000.0, nextFecha(),
                grupoFiltroJuanLeDeben, juanA, grupoFiltroJuanLeDeben.usuarios()
            ).apply { agregarItem("Compra grupal", 1.0, 18000.0) },

            PartesIguales(
                "Filtro - Mixto 1", CategoriaGasto.COMIDA, 9000.0, nextFecha(),
                grupoFiltroPendienteMixto, agustinH, grupoFiltroPendienteMixto.usuarios()
            ).apply { agregarItem("Picada", 1.0, 9000.0) },

            PartesIguales(
                "Filtro - Mixto 2", CategoriaGasto.BEBIDA, 6000.0, nextFecha(),
                grupoFiltroPendienteMixto, juanA, grupoFiltroPendienteMixto.usuarios()
            ).apply { agregarItem("Bebidas", 1.0, 6000.0) },
        ).forEach { gasto ->
            val gastoGuardado = gastoRepository.save(gasto)
            val fracciones = gastoGuardado.repartirGasto()
            val fraccionesExternas = fracciones.filter { it.deudor.id != it.acreedor.id }
            fraccionGastoRepository.saveAll(fraccionesExternas)

            actividadService.registrarActividad(
                usuario = gastoGuardado.pagador,
                tipo = TipoActividad.DEUDA_GENERADA,
                descripcion = "Pagaste ${gastoGuardado.descripcion} en ${gastoGuardado.grupo.nombre}",
                metadata = mapOf(
                    "gastoId" to gastoGuardado.id,
                    "descripcion" to gastoGuardado.descripcion,
                    "monto" to gastoGuardado.monto,
                    "grupoId" to gastoGuardado.grupo.id,
                    "grupoNombre" to gastoGuardado.grupo.nombre,
                )
            )

            fraccionesExternas.forEach { fraccion ->
                actividadService.registrarActividad(
                    usuario = fraccion.deudor,
                    tipo = TipoActividad.GASTO_DEUDA,
                    descripcion = "Debes pagar ${fraccion.montoFraccion} a ${fraccion.acreedor.nombre} por ${gastoGuardado.descripcion}",
                    metadata = mapOf(
                        "gastoId" to gastoGuardado.id,
                        "descripcion" to gastoGuardado.descripcion,
                        "monto" to fraccion.montoFraccion,
                        "acreedor" to fraccion.acreedor.nombre,
                        "grupoId" to gastoGuardado.grupo.id,
                        "grupoNombre" to gastoGuardado.grupo.nombre,
                    )
                )
            }
        }
    }
}