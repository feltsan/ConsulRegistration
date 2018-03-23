package com.feltsan.consulregistration;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MainActivity extends AppCompatActivity {
    private Button get;
    private TextView text;
    private static final String COOKIE_NAME = "Cookie";
    private String homeURL = "https://www.mzv.cz/lvov/uk/x2004_02_03/x2016_05_18/x2017_11_24_1.html";
    private String code = "";
    private Document documentCookie = null, documetPageOfCode = null;
    String cookie = "";
    Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final String path = Environment.getExternalStorageDirectory().getPath();

        get = (Button) findViewById(R.id.getButton);
        text = (TextView) findViewById(R.id.output);
        context = this;
        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               getCookie(path);
            }
        });

    }

    public void getCookie(final String path){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    documentCookie = Jsoup.connect(homeURL)
                            .timeout(10000)
                            .get();

                    if (documentCookie !=null) {

                        Elements scriptElements = documentCookie.getElementsByTag("script");

                        Pattern p = Pattern.compile("(?is)document.cookie=\"(.+?);");
                        Matcher m = p.matcher(scriptElements.html());

                        while (m.find()) {
                            System.out.println("Cookie is " + m.group(1));
                            cookie = m.group(1);
                        }

                        if (!cookie.isEmpty()) {
                            getCode(cookie, path);
                        } else
                            getCookie(path);
                    }else
                        getCookie(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        }

    public String getCode(final String cookie, String path){

                try {
                    documetPageOfCode = Jsoup.connect(homeURL)
                            .header(COOKIE_NAME, cookie)
                            .get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(documetPageOfCode.toString());

                if (documetPageOfCode != null) {

                    code = documetPageOfCode.select("div.article_body").select("li").get(3).select("strong").first().html();

                    System.out.println("CODE is" + code);

                    sendEmail(code,path);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            text.setText(code);
                        }
                    });



//                    Pattern p1 = Pattern.compile("(?is):\"(.+?)<");
//                    Matcher m2 = p1.matcher(elementContainsCode.html());
//
//                    while (m2.find()) {
//                        System.out.println("CODE" + m2.group(1)); // value only
//                    }

                } else {
                    if (!cookie.isEmpty())
                        getCode(cookie,path);
                    else
                        getCookie(path);
                }
                return code;
    }



    public static void sendEmail(String code,String path) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("elvirafeltsan@gmail.com", "efkmAD78");
            }
        });

        try {
            final Message message = new MimeMessage(session);
            String passport = "passport.pdf";
            String contract = "contract.pdf";
            Multipart multipart = new MimeMultipart();
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            String m = "file attached. ";
            messageBodyPart.setText(m, "utf-8", "html");
            multipart.addBodyPart(messageBodyPart);

            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            attachmentBodyPart.attachFile(new File(path+ "/" + passport));
            attachmentBodyPart.attachFile(new File(path +"/"+ contract));
            multipart.addBodyPart(attachmentBodyPart);
            message.setFrom(new InternetAddress("elvirafeltsan@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("ivan.feltsan@gmail.com"));
            message.setSubject(code);
            message.setContent(multipart);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Transport.send(message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();


        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
