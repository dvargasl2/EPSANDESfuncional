package uniandes.edu.co.epsandes.repositorio;
import uniandes.edu.co.epsandes.modelo.AgendarCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AgendarCitaRepository extends JpaRepository<AgendarCita, Long> {
    
    // Buscar citas por afiliado
    List<AgendarCita> findByAfiliadoNumeroDocumento(Long numeroDocumento);
    
    // Buscar citas por médico
    List<AgendarCita> findByMedicoNumeroDocumento(Long numeroDocumento);
    
    // Buscar citas por servicio de salud
    List<AgendarCita> findByServicioDeSaludIdServicio(Long idServicio);
    
    // Buscar citas en un rango de fechas
    List<AgendarCita> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
    
    // Verificar si existe una cita para un afiliado en una fecha y hora específica
    boolean existsByAfiliadoNumeroDocumentoAndFechaHora(Long numeroDocumento, LocalDateTime fechaHora);
    
    // Buscar disponibilidad de citas para un servicio en las próximas 4 semanas
    @Query(value = "SELECT m.NOMBRE as nombre_medico, i.NOMBRE as nombre_ips, " +
           "TO_CHAR(fechas.fecha_base + (horas.hora/24), 'YYYY-MM-DD HH24:MI') as fecha_disponible " +
           "FROM (SELECT TRUNC(SYSDATE) + LEVEL - 1 as fecha_base FROM DUAL CONNECT BY LEVEL <= 28) fechas, " +
           "(SELECT LEVEL + 7 as hora FROM DUAL CONNECT BY LEVEL <= 10) horas, " +
           "SERVICIOSMEDICO sm " +
           "JOIN MEDICO m ON m.NUMERODOCUMENTO = sm.MEDICO_NUMERODOCUMENTO " +
           "JOIN IPS i ON i.NIT = m.IPS_NIT " +
           "WHERE sm.SERVICIO_ID = :servicioId " +
           "AND TO_CHAR(fechas.fecha_base, 'D') BETWEEN '2' AND '6' " +
           "AND NOT EXISTS (SELECT 1 FROM AGENDARCITA ac " +
           "WHERE TO_CHAR(ac.FECHA_HORA, 'YYYY-MM-DD HH24:MI') = " +
           "TO_CHAR(fechas.fecha_base + (horas.hora/24), 'YYYY-MM-DD HH24:MI') " +
           "AND ac.MEDICO_NUMERODOCUMENTO = sm.MEDICO_NUMERODOCUMENTO " +
           "AND ac.SERVICIODESALUD_ID = :servicioId)", 
           nativeQuery = true)
    List<Object[]> findDisponibilidadServicio(@Param("servicioId") Long servicioId);
}