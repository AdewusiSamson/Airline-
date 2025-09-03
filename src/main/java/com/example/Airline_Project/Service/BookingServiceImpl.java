package com.example.Airline_Project.Service;

import com.example.Airline_Project.Repository.BookingRepository;
import com.example.Airline_Project.model.Booking;
import com.example.Airline_Project.model.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;
@Service
public class BookingServiceImpl implements BookingService{
    @Autowired
    private BookingRepository bookingRepository;
    UserService userService;

    @Transactional
    @Override
    public Booking createBooking(Long flightId, String seatNumber, String FirstName,String LastName, User user) {
       Booking  booking =new Booking();
       booking.setStatus(Booking.BookingStatus.CONFIRMED);
       booking.setPassengerFirstName(FirstName);
       booking.setPassengerLastName(LastName);
       booking.setPnr(generatePnr());
       booking.setCreatedAt(LocalDateTime.now());
       booking.setSeatNumber(seatNumber);
       booking.setUser(user);
        return bookingRepository.save(booking);
    }

    private String generatePnr() {
        int pnrLength = 6;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder pnr = new StringBuilder(pnrLength);
        for (int i = 0; i < pnrLength; i++) {
            pnr.append(chars.charAt(random.nextInt(chars.length())));
        }
        return pnr.toString();
    }


    @Override
    public List<Booking> getUserBookings(User user) {
       List<Booking>usersBookings= bookingRepository.findByUser(user);

        return usersBookings;
    }

    @Override
    public Booking getBookingByPnr(String pnr) throws Exception {
        Optional<Booking> booking= bookingRepository.findByPnr(pnr);
        if (booking.isEmpty()){
throw new Exception("no booking found")   ;     }
        return booking.get()
        ;
    }
}
