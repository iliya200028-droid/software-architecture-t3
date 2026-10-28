import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 1. Builder: игровой ПК ===");

        GamingPC budget = new GamingPC.Builder("i5", "RTX 4060")
                .ram(16)
                .build();

        GamingPC top = new GamingPC.Builder("i9", "RTX 5090")
                .ram(64)
                .ssd(2000)
                .liquidCooling(true)
                .rgb(true)
                .build();

        System.out.println("Бюджетный: " + budget);
        System.out.println("Топовый: " + top);

        System.out.println("\n=== 2. Proxy: кэш HTTP-запросов ===");

        HttpService service = new CachingProxy(new RealHttpService());

        for (int i = 1; i <= 2; i++) {
            long start = System.nanoTime();

            String response = service.get("https://example.com");

            double elapsed = (System.nanoTime() - start) / 1_000_000.0;

            System.out.printf(
                    "Запрос %d: %s; время: %.3f мс%n",
                    i, response, elapsed
            );
        }

        System.out.println("\n=== 3. Composite: меню приложения ===");

        Menu root = new Menu("Меню приложения");

        Menu file = new Menu("File");
        file.add(new MenuItem("New", 0))
                .add(new MenuItem("Open", 0))
                .add(new MenuItem("Exit", 0));

        Menu edit = new Menu("Edit");
        edit.add(new MenuItem("Copy", 0))
                .add(new MenuItem("Paste", 0));

        root.add(file).add(edit);
        root.print("");

        System.out.println(
                "Количество пунктов (листьев): " + root.countItems()
        );

        System.out.println("\n=== 4. Facade: бронирование путешествия ===");

        TravelFacade trip = new TravelFacade(
                new FlightBooking(),
                new HotelBooking(false),
                new CarRental()
        );

        trip.planTrip();

        System.out.println("Отпуск не подтверждён. Отменяем путешествие.");
        trip.cancelTrip();

        System.out.println(
                "\nПроверка частичного бронирования: отель недоступен."
        );

        TravelFacade failedTrip = new TravelFacade(
                new FlightBooking(),
                new HotelBooking(true),
                new CarRental()
        );

        try {
            failedTrip.planTrip();
        } catch (IllegalStateException e) {
            System.out.println(
                    "Путешествие не забронировано: " + e.getMessage()
            );
        }

        System.out.println("\n=== 5. Bridge: фигуры и цвета ===");

        Shape[] shapes = {
                new Circle(new Red()),
                new Square(new Blue()),
                new Circle(new Green()),
                new Square(new Red()),
                new Circle(new Blue()),
                new Square(new Green())
        };

        for (Shape shape : shapes) {
            shape.draw();
        }
    }
}

// 1. Builder — пошаговое создание неизменяемого продукта.
abstract class GamingPC {
    private final String cpu;
    private final String gpu;
    private final int ram;
    private final int ssd;
    private final boolean liquidCooling;
    private final boolean rgb;

    private GamingPC(Builder builder) {
        cpu = builder.cpu.trim();
        gpu = builder.gpu.trim();
        ram = builder.ram;
        ssd = builder.ssd;
        liquidCooling = builder.liquidCooling;
        rgb = builder.rgb;
    }

    @Override
    public final String toString() {
        return "CPU: " + cpu
                + ", GPU: " + gpu
                + ", RAM: " + ram + " GB"
                + ", SSD: " + ssd + " GB"
                + ", жидкостное охлаждение: "
                + (liquidCooling ? "да" : "нет")
                + ", RGB: " + (rgb ? "да" : "нет");
    }

    // Закрытая реализация абстрактного продукта.
    private static final class Configuration extends GamingPC {
        private Configuration(Builder builder) {
            super(builder);
        }
    }

    public static final class Builder {
        private final String cpu;
        private final String gpu;

        private int ram = 16;
        private int ssd = 512;
        private boolean liquidCooling;
        private boolean rgb;

        public Builder(String cpu, String gpu) {
            this.cpu = cpu;
            this.gpu = gpu;
        }

        public Builder ram(int value) {
            ram = value;
            return this;
        }

        public Builder ssd(int value) {
            ssd = value;
            return this;
        }

        public Builder liquidCooling(boolean value) {
            liquidCooling = value;
            return this;
        }

        public Builder rgb(boolean value) {
            rgb = value;
            return this;
        }

        public GamingPC build() {
            if (cpu == null || cpu.trim().isEmpty()
                    || gpu == null || gpu.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "CPU и GPU должны быть заполнены"
                );
            }

            if (ram <= 0 || ssd <= 0) {
                throw new IllegalArgumentException(
                        "RAM и SSD должны быть больше нуля"
                );
            }

            return new Configuration(this);
        }
    }
}

// 2. Proxy — кэширующий заместитель HTTP-клиента.
interface HttpService {
    String get(String url);
}

class RealHttpService implements HttpService {
    @Override
    public String get(String url) {
        System.out.println("Загрузка из сети (эмуляция): " + url);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Запрос прерван", e);
        }

        return "Ответ от " + url;
    }
}

class CachingProxy implements HttpService {
    private final HttpService realService;
    private final Map<String, String> cache = new HashMap<>();

    public CachingProxy(HttpService realService) {
        this.realService = Objects.requireNonNull(realService);
    }

    @Override
    public String get(String url) {
        if (cache.containsKey(url)) {
            System.out.println("Ответ из кэша: " + url);
            return cache.get(url);
        }

        String response = realService.get(url);
        cache.put(url, response);

        return response;
    }
}

// 3. Composite — единый интерфейс для пунктов и вложенных меню.
interface MenuComponent {
    void print(String indent);

    int countItems();
}

class MenuItem implements MenuComponent {
    private final String name;
    private final double price;

    public MenuItem(String name, double price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public void print(String indent) {
        System.out.printf("%s%s (%.2f руб.)%n", indent, name, price);
    }

    @Override
    public int countItems() {
        return 1;
    }
}

class Menu implements MenuComponent {
    private final String name;
    private final List<MenuComponent> children = new ArrayList<>();

    public Menu(String name) {
        this.name = name;
    }

    public Menu add(MenuComponent component) {
        children.add(Objects.requireNonNull(component));
        return this;
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + name);

        for (MenuComponent child : children) {
            child.print(indent + "    ");
        }
    }

    @Override
    public int countItems() {
        int total = 0;

        for (MenuComponent child : children) {
            total += child.countItems();
        }

        return total;
    }
}

// 4. Facade — управление бронированием через общий фасад.
class FlightBooking {
    private boolean booked;

    public void book() {
        if (!booked) {
            booked = true;
            System.out.println("Перелёт забронирован");
        }
    }

    public void cancel() {
        if (booked) {
            booked = false;
            System.out.println("Перелёт отменён");
        }
    }
}

class HotelBooking {
    private final boolean simulateFailure;
    private boolean booked;

    public HotelBooking(boolean simulateFailure) {
        this.simulateFailure = simulateFailure;
    }

    public void book() {
        if (simulateFailure) {
            throw new IllegalStateException(
                    "В отеле нет свободных номеров"
            );
        }

        if (!booked) {
            booked = true;
            System.out.println("Отель забронирован");
        }
    }

    public void cancel() {
        if (booked) {
            booked = false;
            System.out.println("Бронирование отеля отменено");
        }
    }
}

class CarRental {
    private boolean booked;

    public void book() {
        if (!booked) {
            booked = true;
            System.out.println("Автомобиль арендован");
        }
    }

    public void cancel() {
        if (booked) {
            booked = false;
            System.out.println("Аренда автомобиля отменена");
        }
    }
}

class TravelFacade {
    private final FlightBooking flight;
    private final HotelBooking hotel;
    private final CarRental car;

    public TravelFacade(
            FlightBooking flight,
            HotelBooking hotel,
            CarRental car
    ) {
        this.flight = Objects.requireNonNull(flight);
        this.hotel = Objects.requireNonNull(hotel);
        this.car = Objects.requireNonNull(car);
    }

    public void planTrip() {
        try {
            flight.book();
            hotel.book();
            car.book();

            System.out.println("Путешествие забронировано");
        } catch (RuntimeException e) {
            System.out.println(
                    "Ошибка бронирования. Откатываем выполненные шаги."
            );

            cancelTrip();
            throw e;
        }
    }

    public void cancelTrip() {
        // Отмена в обратном порядке.
        // Если бронь не создавалась или уже отменена, ничего не происходит.
        car.cancel();
        hotel.cancel();
        flight.cancel();
    }
}

// 5. Bridge — независимые иерархии фигур и цветов.
interface Color {
    void apply(String shape);
}

class Red implements Color {
    @Override
    public void apply(String shape) {
        System.out.println(shape + ": красный цвет");
    }
}

class Blue implements Color {
    @Override
    public void apply(String shape) {
        System.out.println(shape + ": синий цвет");
    }
}

class Green implements Color {
    @Override
    public void apply(String shape) {
        System.out.println(shape + ": зелёный цвет");
    }
}

abstract class Shape {
    protected final Color color;

    protected Shape(Color color) {
        this.color = Objects.requireNonNull(color);
    }

    public abstract void draw();
}

class Circle extends Shape {
    public Circle(Color color) {
        super(color);
    }

    @Override
    public void draw() {
        color.apply("Круг");
    }
}

class Square extends Shape {
    public Square(Color color) {
        super(color);
    }

    @Override
    public void draw() {
        color.apply("Квадрат");
    }
}