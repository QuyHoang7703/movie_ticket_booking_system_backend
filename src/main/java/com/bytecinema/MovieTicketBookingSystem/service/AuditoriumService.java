package com.bytecinema.MovieTicketBookingSystem.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bytecinema.MovieTicketBookingSystem.domain.Auditorium;
import com.bytecinema.MovieTicketBookingSystem.domain.Screening;
import com.bytecinema.MovieTicketBookingSystem.domain.Seat;
import com.bytecinema.MovieTicketBookingSystem.dto.request.auditorium.ReqAddAuditorium;
import com.bytecinema.MovieTicketBookingSystem.dto.response.auditorium.ResAuditoriumDTO;
import com.bytecinema.MovieTicketBookingSystem.dto.response.seat.ResSeatDTO;
import com.bytecinema.MovieTicketBookingSystem.repository.AuditoriumsRepository;
import com.bytecinema.MovieTicketBookingSystem.repository.SeatsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditoriumService {
    private final AuditoriumsRepository auditoriumsRepository;
    private final SeatsRepository seatsRepository;

    
    public ResAuditoriumDTO addAuditorium(ReqAddAuditorium addAuditorium) {
        List<Auditorium> existedAuditorium = auditoriumsRepository.findByNameIgnoreCase(addAuditorium.getName());
        if (!existedAuditorium.isEmpty())
        {
            throw new RuntimeException("Name is used");
        }

        Auditorium auditorium = new Auditorium();
        auditorium.setName(addAuditorium.getName());
        auditorium.setCapacity(addAuditorium.getCapacity());
    
        Auditorium saveAuditorium = auditoriumsRepository.save(auditorium);
    
        // Thêm ghế vào phòng chiếu và lấy danh sách ghế
        List<Seat> seats = addSeatsToAuditorium(saveAuditorium, addAuditorium.getSeatsPerRow());
    
        // Chuyển đổi danh sách ghế thành DTO
        List<ResSeatDTO> seatDTOs = convertToResSeatDTO(seats);
    
        // Tạo đối tượng ResAuditoriumDTO và gán các thông tin
        ResAuditoriumDTO resAuditoriumDTO = new ResAuditoriumDTO();
        resAuditoriumDTO.setId(saveAuditorium.getId());
        resAuditoriumDTO.setCapacity(saveAuditorium.getCapacity());
        resAuditoriumDTO.setName(saveAuditorium.getName());
        resAuditoriumDTO.setSeats(seatDTOs);  // Thêm danh sách ghế vào DTO
    
        return resAuditoriumDTO;
    }

    public void deleteAuditorium(Long id)
    {
        Auditorium auditorium = auditoriumsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Auditorium not found with id: " + id));

        List<Screening> screenings = auditorium.getScreenings();
        if (!screenings.isEmpty())
        {
            throw new RuntimeException("Auditorium is used in screening");
            
        }
        List<Seat> seats = seatsRepository.findByAuditorium(auditorium);
        if (!seats.isEmpty())
        {
            seatsRepository.deleteAll(seats);
        }
        
        auditoriumsRepository.delete(auditorium);
    }
    
    private List<Seat> addSeatsToAuditorium(Auditorium auditorium, int seatsPerRow) {
        int totalSeats = auditorium.getCapacity();
        int fullRows = totalSeats / seatsPerRow;
        List<Seat> seatList = new ArrayList<>();  // Khởi tạo danh sách để lưu các ghế đã tạo

        for (int i = 0; i < totalSeats; i++) {
            Seat seat = new Seat();

            int rowNumber;
            int seatNumber;

            if (i < fullRows * seatsPerRow) {
                rowNumber = i / seatsPerRow;
                seatNumber = i % seatsPerRow + 1;
            } else {
                rowNumber = fullRows;
                seatNumber = i - (fullRows * seatsPerRow) + 1;
            }

            char seatRow = (char) ('A' + rowNumber);

            seat.setSeatNumber(seatNumber);
            seat.setSeatRow(String.valueOf(seatRow));
            seat.setAuditorium(auditorium);

            seatList.add(seat);  // Thêm ghế vào danh sách
            seatsRepository.save(seat);  // Lưu vào database
        }

        return seatList;  // Trả về danh sách các ghế đã tạo
    }

    private List<ResSeatDTO> convertToResSeatDTO(List<Seat> seats) {
        List<ResSeatDTO> seatDTOs = new ArrayList<>();

        for (Seat seat : seats) {
            ResSeatDTO seatDTO = new ResSeatDTO();
            seatDTO.setId(seat.getId());
            seatDTO.setSeatNumber(seat.getSeatNumber());
            seatDTO.setSeatRow(seat.getSeatRow());

            seatDTOs.add(seatDTO);
        }

        return seatDTOs;
    }


    public ResAuditoriumDTO getAuditoriumById(Long id)
    {
        Auditorium auditorium = auditoriumsRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Auditorium not found with id: " + id));

        List<Seat> seats = seatsRepository.findByAuditorium(auditorium);

        List<ResSeatDTO> seatDTOs = convertToResSeatDTO(seats);

        ResAuditoriumDTO resAuditoriumDTO = new ResAuditoriumDTO();
        resAuditoriumDTO.setId(auditorium.getId());
        resAuditoriumDTO.setName(auditorium.getName());
        resAuditoriumDTO.setCapacity(auditorium.getCapacity());
        resAuditoriumDTO.setSeats(seatDTOs); // Thêm danh sách ghế vào DTO

    return resAuditoriumDTO;
    }

    public List<ResAuditoriumDTO> getAuditoriums() {
        List<Auditorium> auditoriums = auditoriumsRepository.findAll();
    
        List<ResAuditoriumDTO> resAuditoriumDTOs = new ArrayList<>();
        for (Auditorium auditorium : auditoriums) {
            ResAuditoriumDTO dto = new ResAuditoriumDTO();
            dto.setId(auditorium.getId());
            dto.setName(auditorium.getName());
            dto.setCapacity(auditorium.getCapacity());
            dto.setSeats(convertToResSeatDTO(seatsRepository.findByAuditorium(auditorium)));
            resAuditoriumDTOs.add(dto);
        }
    
        return resAuditoriumDTOs;
    }
    
    public List<ResAuditoriumDTO> getAuditoriumsByName(String name)
    {
        List<Auditorium> auditoriums = auditoriumsRepository.findByNameStartingWithIgnoreCase(name);
    
        List<ResAuditoriumDTO> resAuditoriumDTOs = new ArrayList<>();
        for (Auditorium auditorium : auditoriums) {
            ResAuditoriumDTO dto = new ResAuditoriumDTO();
            dto.setId(auditorium.getId());
            dto.setName(auditorium.getName());
            dto.setCapacity(auditorium.getCapacity());
            dto.setSeats(convertToResSeatDTO(seatsRepository.findByAuditorium(auditorium)));
            resAuditoriumDTOs.add(dto);
        }
    
        return resAuditoriumDTOs;
    }
    
    
    
}
