package com.clanevents.panels;

import com.clanevents.ClanEventsConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Lookup {
    private final JPanel ssArea = new JPanel();
    private final JLabel ssText = new JLabel();
    private final JComboBox combobox = new JComboBox();
    private PlayerData[] playerData;
    private String color1;
    private String color2;
    private RankData[] ranks;

    public Lookup (ClanEventsConfig config) {

        //Set the color
        color1 = "#"+Integer.toHexString(config.col1color().getRGB()).substring(2);
        color2 = "#"+Integer.toHexString(config.col2color().getRGB()).substring(2);

        combobox.setEditable(true);
        combobox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent event) {
                if (event.getKeyChar() == KeyEvent.VK_ENTER) {
                    String searchString = ((JTextComponent) ((JComboBox) ((Component) event
                            .getSource()).getParent()).getEditor()
                            .getEditorComponent()).getText();
                    if (!searchString.isEmpty()) {
                        GetPlayerData(searchString);
                    }
                }
            }
        });

        combobox.addItemListener(e ->
                {
                    if (e.getStateChange() == ItemEvent.SELECTED)
                    {
                        String selection = e.getItem().toString();
                        PlayerData player = Arrays.stream(playerData).filter(data -> selection.equals(data.getUsername())).findFirst().orElse(null);
                        SetPlayerStats(player);
                    }
                }
        );
        ssArea.setPreferredSize(new Dimension(200, 300));
        ssArea.add(combobox);
        ssArea.add(ssText);

        GetRankData();
    }

    public JPanel getLayout() {
        return ssArea;
    }

    public void GetRankData() {
        try {
            // Create a neat value object to hold the URL
            URL url = new URL("https://infernal-fc.com/api/ranks?_start=0&_end=5000");

            // Open a connection(?) on the URL(??) and cast the response(???)
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Now it's "open", we can set the request method, headers etc.
            connection.setRequestProperty("accept", "application/json");

            // This line makes the request
            InputStream responseStream = connection.getInputStream();

            // Manually converting the response body InputStream to APOD using Jackson
            ObjectMapper mapper = new ObjectMapper();
            ranks = mapper.readValue(responseStream, RankData[].class);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void GetPlayerData(String searchString) {
        try {
            // Create a neat value object to hold the URL
            URL url = new URL("https://infernal-fc.com/api/Members?active=1&_start=0&_end=10&username=" + URLEncoder.encode(searchString, StandardCharsets.UTF_8.toString()));

            // Open a connection(?) on the URL(??) and cast the response(???)
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Now it's "open", we can set the request method, headers etc.
            connection.setRequestProperty("accept", "application/json");

            // This line makes the request
            InputStream responseStream = connection.getInputStream();

            // Manually converting the response body InputStream to APOD using Jackson
            ObjectMapper mapper = new ObjectMapper();
            playerData = mapper.readValue(responseStream, PlayerData[].class);

            String[] dropdownData = Arrays.stream(playerData).map(PlayerData::getUsername).toArray(String[]::new);
            combobox.setModel(new DefaultComboBoxModel(dropdownData));
            combobox.showPopup();

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private void SetPlayerStats(PlayerData playerData) {
        String data = "";

        if (playerData != null) {
            RankData rank = Arrays.stream(ranks).filter(r -> playerData.getRank_id() == r.getId()).findFirst().orElse(null);

            data += "<html><table width=230>";

            data += "<tr>";
            data += "<td><font color='" + color1 + "'>Username</font></td>";
            data += "<td><font color='" + color2 + "'>";
            data += playerData.Username;
            data += "</font></td>";
            data += "</tr>";

            data += "<tr>";
            data += "<td><font color='" + color1 + "'>Rank</font></td>";
            data += "<td><font color='" + color2 + "'>";
            data += rank.Name;
            data += "</font></td>";
            data += "</tr>";

            data += "<tr>";
            data += "<td><font color='" + color1 + "'>PvM Points</font></td>";
            data += "<td><font color='" + color2 + "'>";
            data += playerData.PvmPoints;
            data += "</font></td>";
            data += "</tr>";

            data += "<tr>";
            data += "<td><font color='" + color1 + "'>Community Points</font></td>";
            data += "<td><font color='" + color2 + "'>";
            data += playerData.NonPvmPoints;
            data += "</font></td>";
            data += "</tr>";

            data += "<tr>";
            data += "<td><font color='" + color1 + "'>Total points</font></td>";
            data += "<td><font color='" + color2 + "'>";
            data += playerData.OverallPoints;
            data += "</font></td>";
            data += "</tr>";


            data += "</table></html>";
        }

        ssText.setText(data);
    }
}
