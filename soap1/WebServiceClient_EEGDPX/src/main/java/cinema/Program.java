package cinema;

import seatreservation.*;

import javax.xml.ws.BindingProvider;

public class Program {

	public static void main(String[] args) {
		String url = args[0];
		String row = args[0];
		String column = args[0];
		String task = args[0];

		CinemaService service = new CinemaService();
		ICinema cinema = service.getICinemaHttpSoap11Port();
		((BindingProvider) cinema).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

		try {
			Seat s = new Seat();
			s.setRow(row);
			s.setColumn(column);
			String id = cinema.lock(s, 1);
			switch (task) {
				case "Reserve":
					cinema.reserve(id);
					break;
				case "Buy":
					cinema.buy(id);
					break;
			}
		} catch (ICinemaLockCinemaException | ICinemaReserveCinemaException | ICinemaBuyCinemaException e) {
			e.printStackTrace();
		}
	}
}
