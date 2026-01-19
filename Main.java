import java.util.*;
import java.util.stream.Collectors;

public static void main(String[] args) {
    List<Book> listBook = new ArrayList<>();
    Scanner x = new Scanner(System.in);
    String msg = """
            Chương trình quản lý sách
            1. Thêm 1 cuốn sách
            2. Xóa 1 cuốn sách
            3. Thay đổi sách
            4. Xuất thông tin
            5. Tìm sách Lập trình
            6. Lấy sách tối đa theo giá
            7. Tìm kiếm theo tác giả
            0. Thoát
            Chọn chức năng: """;

    int chon = 0;
    do {
        System.out.print(msg);
        chon = x.nextInt();
        switch (chon) {
            case 1 -> {
                Book newBook = new Book();
                newBook.input();
                listBook.add(newBook);
            }
            case 2 -> {
                System.out.print("Nhập vào mã sách cần xóa: ");
                int bookid = x.nextInt();
                // kiem tra mã sách
                Book find = listBook.stream().filter(p -> p.getId() == bookid).findFirst().orElseThrow();
                listBook.remove(find);
                System.out.print("Đã xóa sách thành công");
            }
            case 3 -> {
                System.out.print("Nhập vào mã sách cần điều chỉnh: ");
                int bookid = x.nextInt();
                Book find = listBook.stream().filter(p -> p.getId() == bookid).findFirst().orElseThrow();
                System.out.println("Nhập thông tin mới cho sách:");
                find.input(); // Gọi phương thức nhập để ghi đè dữ liệu cũ
                System.out.println("Cập nhật thông tin thành công!");
            }
            case 4 -> {
                System.out.println("\n Xuất thông tin danh sách ");
                listBook.forEach(p -> p.output());
            }
            case 5 -> {
                List<Book> list5 = listBook.stream()
                        .filter(u -> u.getTitle().toLowerCase().contains("lập trình"))
                        .toList();
                list5.forEach(Book::output);
            }
            case 6 -> {
                System.out.print("Nhập giá tối đa: ");
                double maxPrice = x.nextDouble();
                List<Book> list6 = listBook.stream()
                        .filter(p -> p.getPrice() <= maxPrice)
                        .limit(5) // Ví dụ lấy tối đa 5 cuốn thỏa điều kiện
                        .toList();
                list6.forEach(Book::output);
            }
            case 7 -> {
                System.out.print("Nhập danh sách tác giả: ");
                x.nextLine();
                String inputAuthors = x.nextLine();

                Set<String> authorSet = Arrays.stream(inputAuthors.split(","))
                        .map(name -> name.trim().toLowerCase())
                        .filter(name -> !name.isEmpty())
                        .collect(Collectors.toSet());

                List<Book> list7 = listBook.stream()
                        .filter(p -> authorSet.contains(p.getAuthor().toLowerCase().trim()))
                        .toList();

                if (list7.isEmpty()) {
                    System.out.println("Không tìm thấy sách của các tác giả đã nhập.");
                } else {
                    System.out.println("\nKết quả tìm kiếm theo tác giả:");
                    list7.forEach(Book::output);
                }
            }
        }
    }while (chon != 0);
}