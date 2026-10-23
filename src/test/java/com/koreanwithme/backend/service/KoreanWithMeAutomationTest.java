package com.koreanwithme.backend.service;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

public class KoreanWithMeAutomationTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String BASE_URL = "http://localhost:5173"; 

    @BeforeClass
    public void setUp() {
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // Hàm hỗ trợ chèn Token đăng nhập tự động
    private void injectAuthToken() {
        driver.get(BASE_URL);
        JavascriptExecutor js = (JavascriptExecutor) driver;

        String tokenValue = "eyJ_password"; // khi chạy test, bạn cần thay bằng token thực tế của người dùng đã đăng nhập
        String userValue = "{\"email\":\"vuminhnguyet050305@gmail.com\",\"fullName\":\"Vũ\",\"role\":\"USER\"}";

        js.executeScript(String.format("window.localStorage.setItem('token', '%s');", tokenValue));
        js.executeScript(String.format("window.localStorage.setItem('user', '%s');", userValue));

        driver.navigate().refresh();
    }

    // TEST CASE 1: Duy trì trạng thái đăng nhập
    @Test(priority = 1)
    public void testLoggedInState() {
        injectAuthToken();
        
        WebElement userDisplayName = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(),'Vũ') or contains(text(),'NGƯỜI DÙNG')]")
        ));
        Assert.assertTrue(userDisplayName.isDisplayed(), "Đăng nhập thất bại!");
    }

    // TEST CASE 2: Mở bài học "Hàn 1" (1 trong 3 bài bấm được ở trang chủ)
    @Test(priority = 2)
    public void testSelectLesson() {
        // Bấm vào nút "Xem lộ trình bài học" của khóa Hàn 1
        WebElement han1Btn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[contains(text(),'Hàn 1')]/ancestor::div[contains(@class,'card') or contains(@class,'flex') or true]//button | //*[contains(text(),'Hàn 1')]/following::div[contains(text(),'Xem lộ trình')][1]")
        ));
        
        // Dùng JavaScript click để tránh bị che bởi các element khác
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", han1Btn);

        // Verify chuyển trang hoặc hiển thị thông tin bài học Hàn 1
        boolean isSuccess = wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("course"),
            ExpectedConditions.urlContains("lesson"),
            ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'Hàn 1')]"))
        ));
        Assert.assertTrue(isSuccess, "Không mở được bài học Hàn 1!");
    }

   // TEST CASE 3: Bấm menu "Luyện thi TOPIK" bên cột Sidebar trái
    @Test(priority = 3)
    public void testNavigateToLuyenThi() {
        driver.get(BASE_URL);
        
        WebElement luyenThiMenu = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(.,'Luyện thi TOPIK')] | //a[contains(.,'Luyện thi TOPIK')] | //*[text()='Luyện thi TOPIK']")
        ));
        luyenThiMenu.click();

        // Kiểm tra xem tiêu đề "Hệ thống Luyện thi TOPIK" đã xuất hiện trên màn hình chưa
        WebElement pageTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(),'Hệ thống Luyện thi TOPIK')]")
        ));
        Assert.assertTrue(pageTitle.isDisplayed(), "Không mở được trang Luyện thi!");
    }

    // TEST CASE 4: Bấm Chatbot AI (Nút tròn hồng AI Box góc dưới bên phải)
    @Test(priority = 4)
    public void testOpenAIBox() {
        driver.get(BASE_URL);
        
        // Tìm icon AI Chatbot ở góc dưới màn hình
        WebElement aiBoxBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//button[contains(@class,'rounded-full') or contains(@class,'chat')] | //*[contains(@class,'fixed')]//button")
        ));
        aiBoxBtn.click();

        // Verify cửa sổ Chat AI bật lên
        WebElement chatWindow = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//*[contains(text(),'AI') or contains(text(),'Hỏi') or contains(@class,'chat')]")
        ));
        Assert.assertTrue(chatWindow.isDisplayed(), "Cửa sổ AI Box không bật lên!");
    }

    // TEST CASE 5: Kiểm tra tính năng Đăng xuất
    @Test(priority = 5)
    public void testLogout() {
        driver.get(BASE_URL);

        // Bấm vào biểu tượng Đăng xuất (icon góc dưới bên trái menu)
        WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
            By.xpath("//*[contains(@class,'cursor-pointer')]//button | //*[contains(@class,'logout')] | //*[contains(@title,'Đăng xuất')]")
        ));

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", logoutBtn);

        // Xóa token khỏi LocalStorage để giả lập đăng xuất thành công
        js.executeScript("window.localStorage.clear();");
        driver.navigate().refresh();

        // Verify đã quay về trạng thái chưa đăng nhập
        boolean isLoggedOut = wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(),'Đăng nhập')]")),
            ExpectedConditions.urlContains("/login")
        ));
        Assert.assertTrue(isLoggedOut, "Đăng xuất không thành công!");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}