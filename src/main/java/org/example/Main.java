package org.example;

import java.io.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


class wiseSaying {
    private int id;
    private String text;
    private String author;

    public wiseSaying(int id, String text, String author) {
        this.id = id;
        this.text = text;
        this.author = author;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return author;
    }


    public void setText(String text) {
        this.text = text;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    @Override
    public String toString() {
        return id + " / " + author + " / " + text;
    }
}

public class Main {
    public static void main(String[] args) {
        App app = new App();
        app.run();
    }
}

// 명언 앱
class App {
    private final String wiseSayingFiles = "db/wiseSaying";
    private final String lastIdFile = wiseSayingFiles + "/lastId.txt";
    int nextId = 1;
    List<wiseSaying> saying = new ArrayList<>();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void run() {
        loadSaying(); // 앱 시작 시 저장된 명언들을 불러오기

        System.out.println("== 명언 앱 ==");
        Scanner scanner = new Scanner(System.in);


        while (true) {
            System.out.print("명령) ");
            String command = scanner.nextLine();

            if (command.equals("종료")) {
                break;
            } else if (command.equals("등록")) {
                register(scanner);
            } else if (command.equals("목록")) {
                display();
            } else if (command.startsWith("삭제?id=")) {
                delete(command);
            } else if (command.startsWith("수정?id=")) {
                modify(command, scanner);
            } else if(command.equals("빌드")){
                build();
            }

        }

        scanner.close();


    }

    // 명언등록
    public void register(Scanner scanner) {
        System.out.print("명언: ");
        String text = scanner.nextLine();
        System.out.print("작가: ");
        String author = scanner.nextLine();

        wiseSaying newWiseSaying = new wiseSaying(nextId++, text, author);
        saying.add(newWiseSaying);
        saveSaying(newWiseSaying);
        saveLastId();

        System.out.println(newWiseSaying.getId() + "번 명언이 등록되었습니다.");

    }

    // 등록된 명언 출력
    public void display() {

        System.out.println("번호 / 작가 / 명언");
        System.out.println("----------------------");
        for (int i = saying.size() - 1; i >= 0; i--) {
            System.out.println(saying.get(i));

        }
    }

    public void delete(String command) {


        int id = Integer.parseInt(command.split("=")[1]);
        boolean error = false;

        for (int i = 0; i < saying.size(); i++) {
            if (saying.get(i).getId() == id) {
                saying.remove(i);
                deleteSayingFile(id);
                System.out.println(id + "번 명언이 삭제되었습니다.");
                error = true;
                break;
            }

        }

        if (!error) {
            System.out.println(id + "번 명언은 존재하지 않습니다.");
        }


    }

    public void modify(String command, Scanner scanner) {

        int id = Integer.parseInt(command.split("=")[1]);
        boolean error = false;

        // for-each 문 사용
        for (wiseSaying sayings : saying) {
            if (sayings.getId() == id) {
                error = true;
                System.out.println("명언(기존): " + sayings.getText());
                System.out.print("명언: ");
                String newText = scanner.nextLine();
                System.out.println("작가(기존): " + sayings.getAuthor());
                System.out.print("작가: ");
                String newAuthor = scanner.nextLine();

                sayings.setText(newText);
                sayings.setAuthor(newAuthor);

                System.out.println(id + "번 명언이 수정되었습니다.");
                break;

            }
        }

        if (!error) {
            System.out.println(id + "번 명언은 존재하지 않습니다.");
        }

    }

    public void saveSaying(wiseSaying wiseSaying) {
        String filePath = wiseSayingFiles + "/" + wiseSaying.getId() + ".json";

        // lastIdFile이 존재하지 않으면 생성
        if (!Files.exists(Paths.get(lastIdFile))) {
            try {
                Files.createFile(Paths.get(lastIdFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            String jsonContent = gson.toJson(wiseSaying);
            Files.writeString(Paths.get(filePath), jsonContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadSaying() {
        // 디렉토리 생성 시 예외 처리
        try {
            Files.createDirectories(Paths.get(wiseSayingFiles)); // 경로에 디렉토리 생성
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadLastId(); // 마지막 ID 로드

        // JSON file들로부터 모든 명언 load
        try (DirectoryStream<Path> files = Files.newDirectoryStream(Paths.get(wiseSayingFiles), "*.json")) {
            for (Path file : files) {
                List<String> content = Files.readAllLines(file);
                String jsonContent = String.join("\n", content);
                wiseSaying loadedSaying = gson.fromJson(jsonContent, wiseSaying.class);
                saying.add(loadedSaying);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    // 마지막 ID 로드
    private void loadLastId() {
        try (BufferedReader br = new BufferedReader(new FileReader(lastIdFile))) {
            nextId = Integer.parseInt(br.readLine().trim());
        } catch (IOException | NumberFormatException e) {
            nextId = 1; // Default to 1 if file doesn't exist or is corrupted
        }
    }

    // 마지막 ID 저장
    private void saveLastId() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(lastIdFile))) {
            bw.write(String.valueOf(nextId));
        } catch (IOException e) {
            System.out.println("Error saving last ID: " + e.getMessage());
        }
    }

    // 명언 파일 삭제
    private void deleteSayingFile(int id) {
        Path path = Paths.get(wiseSayingFiles + "/" + id + ".json");
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 빌드
    private void build(){

        String dataFilePath = "data.json";

        // 명언 리스트를 JSON 형식으로 변환
        try {
            String jsonContent = gson.toJson(saying);
            Files.writeString(Paths.get(dataFilePath), jsonContent);
            System.out.println("data.json 파일의 내용이 갱신되었습니다.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}


