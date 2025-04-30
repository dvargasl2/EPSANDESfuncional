package uniandes.edu.co.epsandes.controller;
import uniandes.edu.co.epsandes.modelo.Medico;
import uniandes.edu.co.epsandes.servicio.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {

    private static final Logger logger = LoggerFactory.getLogger(MedicoController.class);

    @Autowired
    private MedicoService medicoService;

    // RF4 - Registrar médico
    @PostMapping
    public ResponseEntity<Medico> registrarMedico(@RequestBody Medico medico) {
        logger.debug("POST /api/medicos - Body: {}", medico);
        
        // Log adicional para diagnosticar problemas de deserialización
        logger.debug("IPS recibida: {}", (medico.getIps() != null ? 
                 "Objeto IPS presente, NIT=" + medico.getIps().getNit() : 
                 "Objeto IPS es NULL"));
        
        try {
            // Si la IPS es nula, crear un mensaje de error más descriptivo
            if (medico.getIps() == null) {
                logger.warn("La propiedad IPS está ausente en la petición JSON");
                return ResponseEntity.badRequest().header("X-Error", 
                        "El campo IPS es obligatorio. Verifique que la propiedad 'ips' esté presente en el JSON").build();
            }
            
            Medico nuevoMedico = medicoService.registrarMedico(medico);
            logger.info("Médico creado: {}", nuevoMedico);
            return new ResponseEntity<>(nuevoMedico, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            logger.error("Error al registrar médico: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().header("X-Error", e.getMessage()).build();
        }
    }

    // Asignar servicio a médico
    @PostMapping("/{medicoDocumento}/servicios/{servicioId}")
    public ResponseEntity<Void> asignarServicioAMedico(
            @PathVariable Long medicoDocumento,
            @PathVariable Long servicioId) {
        logger.debug("POST /api/medicos/{}/servicios/{}", medicoDocumento, servicioId);
        try {
            medicoService.asignarServicioAMedico(medicoDocumento, servicioId);
            logger.info("Servicio {} asignado a médico {}", servicioId, medicoDocumento);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error al asignar servicio a médico: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Obtener todos los médicos
    @GetMapping
    public ResponseEntity<List<Medico>> obtenerTodosMedicos() {
        List<Medico> medicos = medicoService.obtenerTodosMedicos();
        return new ResponseEntity<>(medicos, HttpStatus.OK);
    }

    // Obtener médico por número de documento
    @GetMapping("/{numeroDocumento}")
    public ResponseEntity<Medico> obtenerMedicoPorDocumento(@PathVariable Long numeroDocumento) {
        try {
            return medicoService.obtenerMedicoPorDocumento(numeroDocumento)
                    .map(medico -> new ResponseEntity<>(medico, HttpStatus.OK))
                    .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener médicos por especialidad
    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<List<Medico>> obtenerMedicosPorEspecialidad(@PathVariable String especialidad) {
        List<Medico> medicos = medicoService.obtenerMedicosPorEspecialidad(especialidad);
        return new ResponseEntity<>(medicos, HttpStatus.OK);
    }

    // Obtener médicos por IPS
    @GetMapping("/ips/{nit}")
    public ResponseEntity<List<Medico>> obtenerMedicosPorIPS(@PathVariable Long nit) {
        List<Medico> medicos = medicoService.obtenerMedicosPorIPS(nit);
        return new ResponseEntity<>(medicos, HttpStatus.OK);
    }

    // Obtener médicos que prestan un servicio específico
    @GetMapping("/servicio/{servicioId}")
    public ResponseEntity<List<Medico>> obtenerMedicosPorServicio(@PathVariable Long servicioId) {
        List<Medico> medicos = medicoService.obtenerMedicosPorServicio(servicioId);
        return new ResponseEntity<>(medicos, HttpStatus.OK);
    }

    // Actualizar médico
    @PutMapping("/{numeroDocumento}")
    public ResponseEntity<Medico> actualizarMedico(
            @PathVariable Long numeroDocumento,
            @RequestBody Medico medico) {
        try {
            if (!medicoService.obtenerMedicoPorDocumento(numeroDocumento).isPresent()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Medico medicoActualizado = medicoService.actualizarMedico(numeroDocumento, medico);
            return new ResponseEntity<>(medicoActualizado, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Eliminar médico
    @DeleteMapping("/{numeroDocumento}")
    public ResponseEntity<Void> eliminarMedico(@PathVariable Long numeroDocumento) {
        try {
            medicoService.eliminarMedico(numeroDocumento);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}