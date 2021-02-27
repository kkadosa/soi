package soap1;

import seatreservation.*;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

@WebService(name="CinemaService",
		portName="ICinema_HttpSoap11_Port",
		targetNamespace="http://www.iit.bme.hu/soi/hw/SeatReservation",
		endpointInterface="seatreservation.ICinema",
		wsdlLocation="WEB-INF/wsdl/SeatReservation.wsdl")
public class Cinema implements ICinema {

	private static final HashMap<Integer, HashMap<Integer, StatusWrapper>> seats = new HashMap<>();
	private static final HashMap<String, ArrayList<StatusWrapper>> reservations = new HashMap<>();
	private static ArrayOfSeat stupidity;
	private static Integer nCol;
	private static Integer nRow;


	@Override
	public void init(int rows, int columns) throws ICinemaInitCinemaException {
		if(rows < 1 || rows > 26) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(1);
			exception.setErrorMessage("Sok sor");
			throw new ICinemaInitCinemaException("Sok sor", exception);
		}
		if(columns < 1 || columns > 100) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(2);
			exception.setErrorMessage("Sok oszlop");
			throw new ICinemaInitCinemaException("Sok oszlop", exception);
		}

		nCol = columns;
		nRow = columns;
		reservations.clear();
		stupidity = new ArrayOfSeat();
		for(int i = 1; i <= rows; ++i) {
			HashMap<Integer, StatusWrapper> row = new HashMap<>();
			seats.put(i, row);
			for(int j = 1; j <= columns; ++j) {
				StatusWrapper wrapper = new StatusWrapper();
				row.put(j, wrapper);
				Seat s = new Seat();
				s.setColumn(Integer.toString(j));
				s.setRow(String.valueOf('A' + i - 1));
				stupidity.getSeat().add(s);
			}
		}
	}

	@Override
	public ArrayOfSeat getAllSeats() {
		return stupidity;
	}

	@Override
	public SeatStatus getSeatStatus(Seat seat) throws ICinemaGetSeatStatusCinemaException {

		Integer rowN = getRow(seat.getRow());
		if(rowN == null) {
				CinemaException exception = new CinemaException();
				exception.setErrorCode(3);
				exception.setErrorMessage("Row Wong");
				throw new ICinemaGetSeatStatusCinemaException("Row Wong", exception);
		}
		HashMap<Integer, StatusWrapper> row = seats.get(rowN);

		String columnS = seat.getColumn();
		if(columnS == null) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(4);
			exception.setErrorMessage("Col Null");
			throw new ICinemaGetSeatStatusCinemaException("Col null", exception);
		}
		int colN;
		try {
			colN = Integer.parseInt(columnS);
		} catch (NumberFormatException e) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(5);
			exception.setErrorMessage("Col not number");
			throw new ICinemaGetSeatStatusCinemaException("Col not number", exception);
		}
		StatusWrapper result = row.get(colN);
		if(result == null) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(6);
			exception.setErrorMessage("No seat");
			throw new ICinemaGetSeatStatusCinemaException("No seat", exception);
		}
		return result.status;
	}

	@Override
	public String lock(Seat seat, int count) throws ICinemaLockCinemaException {
		Integer rowN = getRow(seat.getRow());
		if(rowN == null) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(3);
			exception.setErrorMessage("Row Wong");
			throw new ICinemaLockCinemaException("Row Wong", exception);
		}
		HashMap<Integer, StatusWrapper> row = seats.get(rowN);

		String columnS = seat.getColumn();
		if(columnS == null) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(4);
			exception.setErrorMessage("Col Null");
			throw new ICinemaLockCinemaException("Col null", exception);
		}
		int colN;
		try {
			colN = Integer.parseInt(columnS);
		} catch (NumberFormatException e) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(5);
			exception.setErrorMessage("Col not number");
			throw new ICinemaLockCinemaException("Col not number", exception);
		}
		if(colN < 1 || colN > nCol) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(6);
			exception.setErrorMessage("No seat");
			throw new ICinemaLockCinemaException("No seat", exception);
		}
		if(colN + count > nCol) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(7);
			exception.setErrorMessage("Count too big");
			throw new ICinemaLockCinemaException("Count too big", exception);
		}

		ArrayList<StatusWrapper> candy = new ArrayList<>();
		for(int i = colN; i < colN + count; ++i) {
			StatusWrapper c = row.get(i);
			if(SeatStatus.FREE.equals(c.status)) {
				candy.add(c);
			} else {
				CinemaException exception = new CinemaException();
				exception.setErrorCode(8);
				exception.setErrorMessage("foglalt");
				throw new ICinemaLockCinemaException("foglalt", exception);
			}
		}
		for(StatusWrapper c : candy) {
			c.status = SeatStatus.LOCKED;
		}
		String key = UUID.randomUUID().toString();
		reservations.put(key, candy);
		return key;
	}

	@Override
	public void unlock(String lockId) throws ICinemaUnlockCinemaException {
		if(lockId == null) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(9);
			exception.setErrorMessage("id null");
			throw new ICinemaUnlockCinemaException("id null", exception);
		}
		ArrayList<StatusWrapper> array = reservations.get(lockId);
		if(array == null) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(10);
			exception.setErrorMessage("id nonsense");
			throw new ICinemaUnlockCinemaException("id nonsense", exception);
		}
		ArrayList<StatusWrapper> candy = new ArrayList<>();
		for(StatusWrapper i : array) {
			if(SeatStatus.LOCKED.equals(i.status)) {
				candy.add(i);
			} else {
				CinemaException exception = new CinemaException();
				exception.setErrorCode(11);
				exception.setErrorMessage("unauthorized");
				throw new ICinemaUnlockCinemaException("unauthorized", exception);
			}
		}
		for(StatusWrapper i : candy) {
			i.status = SeatStatus.FREE;
		}
	}

	@Override
	public void reserve(String lockId) throws ICinemaReserveCinemaException {
		if(lockId == null) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(9);
			exception.setErrorMessage("id null");
			throw new ICinemaReserveCinemaException("id null", exception);
		}
		ArrayList<StatusWrapper> array = reservations.get(lockId);
		if(array == null) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(10);
			exception.setErrorMessage("id nonsense");
			throw new ICinemaReserveCinemaException("id nonsense", exception);
		}
		for(StatusWrapper i : array) {
			i.status = SeatStatus.RESERVED;
		}
	}

	@Override
	public void buy(String lockId) throws ICinemaBuyCinemaException {
		if(lockId == null) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(9);
			exception.setErrorMessage("id null");
			throw new ICinemaBuyCinemaException("id null", exception);
		}
		ArrayList<StatusWrapper> array = reservations.get(lockId);
		if(array == null) {
			CinemaException exception = new CinemaException();
			exception.setErrorCode(10);
			exception.setErrorMessage("id nonsense");
			throw new ICinemaBuyCinemaException("id nonsense", exception);
		}
		for(StatusWrapper i : array) {
			i.status = SeatStatus.SOLD;
		}
	}

	private Integer getRow(String rowS) {
		if(rowS != null) {
			if(rowS.length() == 1) {
				char cha = rowS.charAt(0);
				int candy = cha - 64;
				if(candy > 0 && candy <= nRow) {
					return candy;
				}
			}
		}
		return null;
	}

	private static class StatusWrapper {
		SeatStatus status = SeatStatus.FREE;
	}
}
