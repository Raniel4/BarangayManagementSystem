/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.barangaymanagementsystem;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;

//imports for account creation
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JOptionPane;

//imports for username update
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//imports for password update
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author My Computer
 */
public class Home extends javax.swing.JFrame {

    private static String loggedInUserPassword;

    public static void setLoggedInUserPassword(String password) {
        loggedInUserPassword = password;
    }

    public static String getLoggedInUserPassword() {
        return loggedInUserPassword;
    }

    /**
     * Creates new form Home
     */
    public Home() {
        getContentPane().setBackground(new java.awt.Color(254, 250, 224));

        initComponents();
        curDateTime();
        attachButtonListeners();
        setupDeleteButton();
        modifyTableModel();
        updateTable();
        
    }

    /*
        start of Create Account Code
     */
    private boolean usernameExists(String username) {
        return usernameExistsInFile(username, "admin.txt") || usernameExistsInFile(username, "user.txt");
    }

    private boolean usernameExistsInFile(String username, String filename) {
        Path filePath = Paths.get(filename);

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(username + " ||")) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void attachButtonListeners() {
        CreateAccount.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                String username = jTextField1.getText().trim();
                String password = jTextField2.getText().trim();
                String confirmPassword = jTextField3.getText().trim();

                String usernameRegex = "^[A-Za-z]{2,}$";
                String passwordRegex = "^.{6,}$";

                Pattern patternUsername = Pattern.compile(usernameRegex);
                Matcher matcherUsername = patternUsername.matcher(username);

                Pattern patternPassword = Pattern.compile(passwordRegex);
                Matcher matcherPassword = patternPassword.matcher(password);

                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!matcherUsername.matches()) {
                    JOptionPane.showMessageDialog(null, "The username must be at least 2 characters long and contain only letters.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!matcherPassword.matches()) {
                    JOptionPane.showMessageDialog(null, "The password must be at least 6 characters long.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    JOptionPane.showMessageDialog(null, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String userTypeSelected = userType.getSelectedItem().toString();

                if (usernameExists(username)) {
                    JOptionPane.showMessageDialog(null, "An account with this username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String filename = userTypeSelected.equals("Admin") ? "admin.txt" : "user.txt";

                try (FileWriter writer = new FileWriter(filename, true)) {
                    writer.write(username + " || " + password + System.lineSeparator());
                    writer.flush();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error occurred while saving account.", "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    return;
                }

                JOptionPane.showMessageDialog(null, "Account successfully added!", "Success", JOptionPane.INFORMATION_MESSAGE);
                jTextField1.setText("");
                jTextField2.setText("");
                jTextField3.setText("");
            }
        });
    }

    /*
        end of Create Account Code
     */
    private boolean updateUsernameInFile(String filename, String oldUsername, String newUsername) {
        File file = new File(filename);
        File tempFile = new File(file.getAbsolutePath() + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(file)); BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            boolean foundAndUpdated = false;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(oldUsername + " || ")) {
                    line = line.replaceFirst(Pattern.quote(oldUsername), Matcher.quoteReplacement(newUsername));
                    foundAndUpdated = true;
                }
                writer.write(line + System.lineSeparator());
            }

            if (foundAndUpdated) {
                writer.close();
                reader.close();
                if (file.delete()) {
                    tempFile.renameTo(file);
                } else {
                    JOptionPane.showMessageDialog(null, "Could not delete the old file.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                tempFile.delete();
            }

            return foundAndUpdated;
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while updating the username.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        return false;
    }

    /*
        end of ChangeUsername Code
     */

 /*
        start of ChangePassword Code
     */
    private boolean updatePasswordInFile(String filename, String username, String oldPassword, String newPassword) {
        Path filePath = Paths.get(filename);
        Path tempFilePath = Paths.get(filePath.toString() + ".tmp");
        boolean foundAndUpdated = false;

        try (BufferedReader reader = Files.newBufferedReader(filePath); BufferedWriter writer = Files.newBufferedWriter(tempFilePath)) {

            String line;

            while ((line = reader.readLine()) != null) {
                String[] userDetails = line.split(" \\|\\| ");
                if (userDetails.length == 2 && userDetails[0].equals(username) && userDetails[1].equals(oldPassword)) {
                    line = username + " || " + newPassword;
                    foundAndUpdated = true;
                }
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        if (foundAndUpdated) {
            try {
                Files.delete(filePath);
                Files.move(tempFilePath, filePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try {
                Files.delete(tempFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return foundAndUpdated;
    }

    private void tryUpdatePassword(String username, String oldPassword, String newPassword) {
        boolean updatedUser = updatePasswordInFile("user.txt", username, oldPassword, newPassword);
        boolean updatedAdmin = updatePasswordInFile("admin.txt", username, oldPassword, newPassword);

        if (updatedUser || updatedAdmin) {
            JOptionPane.showMessageDialog(null, "Password successfully updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "The username does not exist or the old password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /*
        end of ChangePassword Code
     */

 /*
        start of delete account code
     */
    private void modifyTableModel() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

        if (model.getColumnCount() > 3) {
            TableColumn col = jTable1.getColumnModel().getColumn(3);
            jTable1.removeColumn(col);
            model.setColumnCount(3);
        }

        model.setColumnIdentifiers(new Object[]{"Username", "Account Type", "Select"});
        jTable1.setModel(new DefaultTableModel(null, new Object[]{"Username", "Account Type", "Select"}) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) {
                    return Boolean.class;
                } else {
                    return String.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        });

        for (int i = 0; i < model.getRowCount(); i++) {
            ((DefaultTableModel) jTable1.getModel()).addRow(new Object[]{
                model.getValueAt(i, 0), // Username
                model.getValueAt(i, 1), // Account Type
                false // Checkbox state
            });
        }

        if (jTable1.getColumnModel().getColumnCount() > 2) {
            jTable1.getColumnModel().getColumn(2).setPreferredWidth(5);
        }
    }

    private class CustomCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(getFont().deriveFont(Font.BOLD, 16f));
            setForeground(Color.WHITE);
            return this;
        }
    }

    private void loadAccountsIntoTable() {
        jTable1.setDefaultRenderer(Object.class, new CustomCellRenderer());
        jTable1.setRowHeight(30);

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

        model.setRowCount(0);

        loadAccountsFromFile("admin.txt", "Admin", model);
        loadAccountsFromFile("user.txt", "User", model);
    }

    private void loadAccountsFromFile(String fileName, String userType, DefaultTableModel model) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" \\|\\| ");
                if (parts.length >= 1) {
                    model.addRow(new Object[]{parts[0], userType});
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteSelectedAccounts() {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            Boolean checked = (Boolean) model.getValueAt(i, 2);
            if (Boolean.TRUE.equals(checked)) {
                String username = (String) model.getValueAt(i, 0); // Username
                String accountType = (String) model.getValueAt(i, 1); // Account type

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete " + username + " (" + accountType + ")?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteAccount(username, accountType.equals("Admin") ? "admin.txt" : "user.txt");
                    model.removeRow(i);
                }
            }
        }
    }

    private void deleteAccount(String username, String filename) {
        File file = new File(filename);
        File tempFile = new File(file.getAbsolutePath() + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(file)); BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(username + " ||")) {
                    writer.write(line + System.lineSeparator());
                }
            }

            writer.close();
            reader.close();

            if (!file.delete() || !tempFile.renameTo(file)) {
                JOptionPane.showMessageDialog(null, "Error occurred while deleting account.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void deleteReservationRequest(String name, String date, String time, String refNum, String filename) {
        File file = new File(filename);
        File tempFile = new File(file.getAbsolutePath() + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(file)); BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(name + "," + date + "," + time + "," + refNum)) {
                    writer.write(line + System.lineSeparator());
                }
            }

            writer.close();
            reader.close();

            if (!file.delete() || !tempFile.renameTo(file)) {
                JOptionPane.showMessageDialog(null, "Error occurred while deleting account.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private class DeleteButtonActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            deleteSelectedAccounts();
        }
    }

    public void setupDeleteButton() {
        DeleteButton.addActionListener(new DeleteButtonActionListener());
    }

    /*
        end of delete account code
     */

 /*
        start of Username Code
     */
    public void setLoggedInUsername(String username) {
        jLabel9.setText(username);
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    }

    /*
        end of Username Code
     */
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        SideBar = new javax.swing.JPanel();
        accountsButton = new javax.swing.JButton();
        complaintsButton = new javax.swing.JButton();
        idCardButton = new javax.swing.JButton();
        clearanceButton = new javax.swing.JButton();
        reservationButton = new javax.swing.JButton();
        residentsButton = new javax.swing.JButton();
        logOutButton = new javax.swing.JButton();
        homeButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        accountsPanel = new javax.swing.JPanel();
        createAccountButton = new javax.swing.JButton();
        updateAccountButton = new javax.swing.JButton();
        deleteAccountButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        createAccount = new javax.swing.JPanel();
        backButton = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        userType = new javax.swing.JComboBox<>();
        CreateAccount = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        update = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        changeUsername1 = new javax.swing.JButton();
        changePassword1 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        changeUsername = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        changeUsernameButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        changePassword = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jTextField7 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        jTextField9 = new javax.swing.JTextField();
        changePasswordButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        delete = new javax.swing.JPanel();
        jButton4 = new javax.swing.JButton();
        DeleteButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        welcomeAdmin = new javax.swing.JPanel();
        dateTime = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        totalResidentCounter = new javax.swing.JLabel();
        registeredVotersCounter = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        residents = new javax.swing.JPanel();
        addNewResi = new javax.swing.JButton();
        updateResi = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        addResident = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jTextField10 = new javax.swing.JTextField();
        addNewResidentButton = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox<>();
        jTextField11 = new javax.swing.JTextField();
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel12 = new javax.swing.JLabel();
        removeResident = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        residentsTable = new javax.swing.JTable();
        removeResidentButton = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        reservations = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        acceptedReservationList = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        reservationList = new javax.swing.JTable();
        rejectReservationRequest = new javax.swing.JButton();
        acceptReservationRequest = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(254, 250, 224));
        setBounds(new java.awt.Rectangle(0, 0, 0, 0));
        setPreferredSize(new java.awt.Dimension(1210, 930));
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        SideBar.setBackground(new java.awt.Color(40, 54, 24));
        SideBar.setPreferredSize(new java.awt.Dimension(270, 520));
        SideBar.setLayout(null);

        accountsButton.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        accountsButton.setForeground(new java.awt.Color(255, 255, 255));
        accountsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonIdle.png"))); // NOI18N
        accountsButton.setText("ACCOUNTS");
        accountsButton.setBorderPainted(false);
        accountsButton.setContentAreaFilled(false);
        accountsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        accountsButton.setIconTextGap(-120);
        accountsButton.setInheritsPopupMenu(true);
        accountsButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        accountsButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonPressed.png"))); // NOI18N
        accountsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                accountsButtonActionPerformed(evt);
            }
        });
        SideBar.add(accountsButton);
        accountsButton.setBounds(25, 227, 150, 60);

        complaintsButton.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        complaintsButton.setForeground(new java.awt.Color(255, 255, 255));
        complaintsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonIdle.png"))); // NOI18N
        complaintsButton.setText("COMPLAINTS");
        complaintsButton.setBorderPainted(false);
        complaintsButton.setContentAreaFilled(false);
        complaintsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        complaintsButton.setIconTextGap(-128);
        complaintsButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        complaintsButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonPressed.png"))); // NOI18N
        complaintsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                complaintsButtonActionPerformed(evt);
            }
        });
        SideBar.add(complaintsButton);
        complaintsButton.setBounds(25, 317, 150, 60);

        idCardButton.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        idCardButton.setForeground(new java.awt.Color(255, 255, 255));
        idCardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonIdle.png"))); // NOI18N
        idCardButton.setText("ID CARD");
        idCardButton.setBorderPainted(false);
        idCardButton.setContentAreaFilled(false);
        idCardButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        idCardButton.setIconTextGap(-108);
        idCardButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        idCardButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonPressed.png"))); // NOI18N
        SideBar.add(idCardButton);
        idCardButton.setBounds(25, 407, 150, 60);

        clearanceButton.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        clearanceButton.setForeground(new java.awt.Color(255, 255, 255));
        clearanceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonIdle.png"))); // NOI18N
        clearanceButton.setText("CLEARANCE");
        clearanceButton.setBorderPainted(false);
        clearanceButton.setContentAreaFilled(false);
        clearanceButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        clearanceButton.setIconTextGap(-125);
        clearanceButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        clearanceButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonPressed.png"))); // NOI18N
        SideBar.add(clearanceButton);
        clearanceButton.setBounds(25, 495, 150, 60);

        reservationButton.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        reservationButton.setForeground(new java.awt.Color(255, 255, 255));
        reservationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonIdle.png"))); // NOI18N
        reservationButton.setText("RESERVATION");
        reservationButton.setBorderPainted(false);
        reservationButton.setContentAreaFilled(false);
        reservationButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        reservationButton.setIconTextGap(-133);
        reservationButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        reservationButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonPressed.png"))); // NOI18N
        reservationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reservationButtonActionPerformed(evt);
            }
        });
        SideBar.add(reservationButton);
        reservationButton.setBounds(25, 584, 150, 60);

        residentsButton.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        residentsButton.setForeground(new java.awt.Color(255, 255, 255));
        residentsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonIdle.png"))); // NOI18N
        residentsButton.setText("RESIDENTS");
        residentsButton.setBorderPainted(false);
        residentsButton.setContentAreaFilled(false);
        residentsButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        residentsButton.setIconTextGap(-120);
        residentsButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        residentsButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonPressed.png"))); // NOI18N
        residentsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                residentsButtonActionPerformed(evt);
            }
        });
        SideBar.add(residentsButton);
        residentsButton.setBounds(25, 674, 150, 60);

        logOutButton.setFont(new java.awt.Font("Arial Black", 1, 14)); // NOI18N
        logOutButton.setForeground(new java.awt.Color(255, 255, 255));
        logOutButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonIdle.png"))); // NOI18N
        logOutButton.setText("LOG OUT");
        logOutButton.setBorderPainted(false);
        logOutButton.setContentAreaFilled(false);
        logOutButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logOutButton.setIconTextGap(-112);
        logOutButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        logOutButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonPressed.png"))); // NOI18N
        logOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logOutButtonActionPerformed(evt);
            }
        });
        SideBar.add(logOutButton);
        logOutButton.setBounds(25, 820, 150, 60);

        homeButton.setBackground(new java.awt.Color(40, 54, 24));
        homeButton.setBorderPainted(false);
        homeButton.setContentAreaFilled(false);
        homeButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        homeButton.setFocusPainted(false);
        homeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                homeButtonActionPerformed(evt);
            }
        });
        SideBar.add(homeButton);
        homeButton.setBounds(35, 40, 130, 120);

        jLabel1.setBackground(new java.awt.Color(40, 54, 24));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/sidebarblank.png"))); // NOI18N
        jLabel1.setFocusable(false);
        SideBar.add(jLabel1);
        jLabel1.setBounds(0, 0, 200, 900);

        getContentPane().add(SideBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 900));

        accountsPanel.setLayout(null);

        createAccountButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/createAidle.png"))); // NOI18N
        createAccountButton.setBorderPainted(false);
        createAccountButton.setContentAreaFilled(false);
        createAccountButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        createAccountButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/createApressed.png"))); // NOI18N
        createAccountButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createAccountButtonActionPerformed(evt);
            }
        });
        accountsPanel.add(createAccountButton);
        createAccountButton.setBounds(90, 280, 235, 400);

        updateAccountButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/updateAidle.png"))); // NOI18N
        updateAccountButton.setBorderPainted(false);
        updateAccountButton.setContentAreaFilled(false);
        updateAccountButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        updateAccountButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/updateApressed.png"))); // NOI18N
        updateAccountButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateAccountButtonActionPerformed(evt);
            }
        });
        accountsPanel.add(updateAccountButton);
        updateAccountButton.setBounds(383, 280, 235, 400);

        deleteAccountButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/deleteAidle.png"))); // NOI18N
        deleteAccountButton.setBorderPainted(false);
        deleteAccountButton.setContentAreaFilled(false);
        deleteAccountButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        deleteAccountButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/deleteApressed.png"))); // NOI18N
        deleteAccountButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAccountButtonActionPerformed(evt);
            }
        });
        accountsPanel.add(deleteAccountButton);
        deleteAccountButton.setBounds(693, 280, 235, 400);

        jLabel2.setFont(new java.awt.Font("Arial Black", 1, 36)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/accounts.png"))); // NOI18N
        accountsPanel.add(jLabel2);
        jLabel2.setBounds(0, 0, 1000, 900);

        jTabbedPane1.addTab("tab1", accountsPanel);

        createAccount.setLayout(null);

        backButton.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        backButton.setForeground(new java.awt.Color(255, 255, 255));
        backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonIdle.png"))); // NOI18N
        backButton.setText("BACK");
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        backButton.setIconTextGap(-80);
        backButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        backButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonPressed.png"))); // NOI18N
        backButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });
        createAccount.add(backButton);
        backButton.setBounds(868, 188, 100, 60);

        jTextField1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        createAccount.add(jTextField1);
        jTextField1.setBounds(350, 310, 290, 30);

        jTextField2.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        createAccount.add(jTextField2);
        jTextField2.setBounds(350, 440, 290, 30);

        jTextField3.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        createAccount.add(jTextField3);
        jTextField3.setBounds(350, 580, 290, 30);

        userType.setBackground(new java.awt.Color(96, 108, 56));
        userType.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        userType.setForeground(new java.awt.Color(255, 255, 255));
        userType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "User", "Admin" }));
        userType.setFocusable(false);
        createAccount.add(userType);
        userType.setBounds(440, 670, 120, 35);

        CreateAccount.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        CreateAccount.setForeground(new java.awt.Color(255, 255, 255));
        CreateAccount.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/longButtonIdle.png"))); // NOI18N
        CreateAccount.setText("CREATE ACCOUNT");
        CreateAccount.setBorderPainted(false);
        CreateAccount.setContentAreaFilled(false);
        CreateAccount.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        CreateAccount.setIconTextGap(-220);
        CreateAccount.setMargin(new java.awt.Insets(0, 0, 0, 0));
        CreateAccount.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/longButtonPressed.png"))); // NOI18N
        CreateAccount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CreateAccountActionPerformed(evt);
            }
        });
        createAccount.add(CreateAccount);
        CreateAccount.setBounds(375, 810, 250, 60);

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/createAccount.png"))); // NOI18N
        createAccount.add(jLabel3);
        jLabel3.setBounds(0, 0, 1000, 900);

        jTabbedPane1.addTab("tab2", createAccount);

        update.setLayout(null);

        jButton1.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonIdle.png"))); // NOI18N
        jButton1.setText("BACK");
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton1.setIconTextGap(-80);
        jButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton1.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonPressed.png"))); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });
        update.add(jButton1);
        jButton1.setBounds(870, 190, 100, 60);

        changeUsername1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/chngeUidle.png"))); // NOI18N
        changeUsername1.setBorderPainted(false);
        changeUsername1.setContentAreaFilled(false);
        changeUsername1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        changeUsername1.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/chngeUpressed.png"))); // NOI18N
        changeUsername1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeUsername1ActionPerformed(evt);
            }
        });
        update.add(changeUsername1);
        changeUsername1.setBounds(213, 280, 235, 400);

        changePassword1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/chngePidle.png"))); // NOI18N
        changePassword1.setBorderPainted(false);
        changePassword1.setContentAreaFilled(false);
        changePassword1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        changePassword1.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/chngePpressed.png"))); // NOI18N
        changePassword1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changePassword1ActionPerformed(evt);
            }
        });
        update.add(changePassword1);
        changePassword1.setBounds(553, 280, 235, 400);

        jLabel4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/accounts.png"))); // NOI18N
        update.add(jLabel4);
        jLabel4.setBounds(0, 0, 1000, 900);

        jTabbedPane1.addTab("tab3", update);

        changeUsername.setLayout(null);

        jButton3.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonIdle.png"))); // NOI18N
        jButton3.setText("BACK");
        jButton3.setBorderPainted(false);
        jButton3.setContentAreaFilled(false);
        jButton3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton3.setIconTextGap(-80);
        jButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton3.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonPressed.png"))); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        changeUsername.add(jButton3);
        jButton3.setBounds(868, 188, 100, 60);

        jTextField4.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        changeUsername.add(jTextField4);
        jTextField4.setBounds(350, 310, 290, 30);

        jTextField5.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        changeUsername.add(jTextField5);
        jTextField5.setBounds(350, 440, 290, 30);

        jTextField6.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        changeUsername.add(jTextField6);
        jTextField6.setBounds(350, 580, 290, 30);

        changeUsernameButton.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        changeUsernameButton.setForeground(new java.awt.Color(255, 255, 255));
        changeUsernameButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/longButtonIdle.png"))); // NOI18N
        changeUsernameButton.setText("CHANGE USERNAME");
        changeUsernameButton.setBorderPainted(false);
        changeUsernameButton.setContentAreaFilled(false);
        changeUsernameButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        changeUsernameButton.setIconTextGap(-235);
        changeUsernameButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        changeUsernameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeUsernameButtonActionPerformed(evt);
            }
        });
        changeUsername.add(changeUsernameButton);
        changeUsernameButton.setBounds(375, 810, 250, 60);

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/changeUsername.png"))); // NOI18N
        changeUsername.add(jLabel5);
        jLabel5.setBounds(0, 0, 1000, 900);

        jTabbedPane1.addTab("tab4", changeUsername);

        changePassword.setLayout(null);

        jButton2.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonIdle.png"))); // NOI18N
        jButton2.setText("BACK");
        jButton2.setBorderPainted(false);
        jButton2.setContentAreaFilled(false);
        jButton2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton2.setIconTextGap(-80);
        jButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton2.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonPressed.png"))); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        changePassword.add(jButton2);
        jButton2.setBounds(868, 188, 100, 60);

        jTextField7.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        changePassword.add(jTextField7);
        jTextField7.setBounds(350, 310, 290, 30);

        jTextField8.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        changePassword.add(jTextField8);
        jTextField8.setBounds(350, 440, 290, 30);

        jTextField9.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        changePassword.add(jTextField9);
        jTextField9.setBounds(350, 580, 290, 30);

        changePasswordButton.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        changePasswordButton.setForeground(new java.awt.Color(255, 255, 255));
        changePasswordButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/longButtonIdle.png"))); // NOI18N
        changePasswordButton.setText("CHANGE PASSWORD");
        changePasswordButton.setBorderPainted(false);
        changePasswordButton.setContentAreaFilled(false);
        changePasswordButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        changePasswordButton.setIconTextGap(-232);
        changePasswordButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        changePasswordButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/longButtonPressed.png"))); // NOI18N
        changePasswordButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changePasswordButtonActionPerformed(evt);
            }
        });
        changePassword.add(changePasswordButton);
        changePasswordButton.setBounds(375, 810, 250, 60);

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/changePassword.png"))); // NOI18N
        changePassword.add(jLabel6);
        jLabel6.setBounds(0, 0, 1000, 900);

        jTabbedPane1.addTab("tab5", changePassword);

        delete.setLayout(null);

        jButton4.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonIdle.png"))); // NOI18N
        jButton4.setText("BACK");
        jButton4.setBorderPainted(false);
        jButton4.setContentAreaFilled(false);
        jButton4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton4.setIconTextGap(-80);
        jButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton4.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonPressed.png"))); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backButtonActionPerformed(evt);
            }
        });
        delete.add(jButton4);
        jButton4.setBounds(868, 188, 100, 60);

        DeleteButton.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        DeleteButton.setForeground(new java.awt.Color(255, 255, 255));
        DeleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonIdle.png"))); // NOI18N
        DeleteButton.setText("Delete");
        DeleteButton.setBorderPainted(false);
        DeleteButton.setContentAreaFilled(false);
        DeleteButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        DeleteButton.setIconTextGap(-80);
        DeleteButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        DeleteButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonPressed.png"))); // NOI18N
        DeleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeleteButtonbackButtonActionPerformed(evt);
            }
        });
        delete.add(DeleteButton);
        DeleteButton.setBounds(370, 810, 260, 60);

        jTable1.setBackground(new java.awt.Color(96, 108, 56));
        jTable1.setFont(new java.awt.Font("Arial Black", 0, 12)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Username", "Account Type"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setGridColor(new java.awt.Color(255, 255, 255));
        jTable1.setSelectionBackground(new java.awt.Color(64, 74, 36));
        jTable1.setSelectionForeground(new java.awt.Color(255, 255, 255));
        jScrollPane1.setViewportView(jTable1);

        delete.add(jScrollPane1);
        jScrollPane1.setBounds(20, 280, 860, 520);

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/delete.png"))); // NOI18N
        delete.add(jLabel7);
        jLabel7.setBounds(0, 0, 1000, 900);

        jTabbedPane1.addTab("tab6", delete);

        welcomeAdmin.setLayout(null);

        dateTime.setFont(new java.awt.Font("Arial", 1, 24)); // NOI18N
        dateTime.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        dateTime.setText("date and time here");
        welcomeAdmin.add(dateTime);
        dateTime.setBounds(740, 180, 240, 50);

        jLabel9.setFont(new java.awt.Font("Yu Mincho Demibold", 1, 40)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("username");
        welcomeAdmin.add(jLabel9);
        jLabel9.setBounds(10, 220, 360, 90);

        jLabel11.setFont(new java.awt.Font("Yu Mincho Demibold", 1, 40)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Welcome!");
        welcomeAdmin.add(jLabel11);
        jLabel11.setBounds(10, 170, 360, 90);

        totalResidentCounter.setFont(new java.awt.Font("Arial Black", 1, 48)); // NOI18N
        totalResidentCounter.setForeground(new java.awt.Color(255, 255, 255));
        totalResidentCounter.setText("0");
        welcomeAdmin.add(totalResidentCounter);
        totalResidentCounter.setBounds(280, 480, 80, 68);

        registeredVotersCounter.setFont(new java.awt.Font("Arial Black", 1, 48)); // NOI18N
        registeredVotersCounter.setForeground(new java.awt.Color(255, 255, 255));
        registeredVotersCounter.setText("0");
        welcomeAdmin.add(registeredVotersCounter);
        registeredVotersCounter.setBounds(280, 710, 80, 68);

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/welcomeAdmin.png"))); // NOI18N
        welcomeAdmin.add(jLabel8);
        jLabel8.setBounds(0, 0, 1000, 900);

        jTabbedPane1.addTab("tab7", welcomeAdmin);

        residents.setLayout(null);

        addNewResi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/addResiIdle.png"))); // NOI18N
        addNewResi.setBorderPainted(false);
        addNewResi.setContentAreaFilled(false);
        addNewResi.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addNewResi.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/addResiPressed.png"))); // NOI18N
        addNewResi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewResiActionPerformed(evt);
            }
        });
        residents.add(addNewResi);
        addNewResi.setBounds(213, 280, 235, 400);

        updateResi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/removeResIdle.png"))); // NOI18N
        updateResi.setBorderPainted(false);
        updateResi.setContentAreaFilled(false);
        updateResi.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        updateResi.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/removeResPressed.png"))); // NOI18N
        updateResi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateResiActionPerformed(evt);
            }
        });
        residents.add(updateResi);
        updateResi.setBounds(553, 280, 235, 400);

        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/residents.png"))); // NOI18N
        residents.add(jLabel10);
        jLabel10.setBounds(0, 0, 1000, 900);

        jTabbedPane1.addTab("tab8", residents);

        addResident.setLayout(null);

        jButton5.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        jButton5.setForeground(new java.awt.Color(255, 255, 255));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonIdle.png"))); // NOI18N
        jButton5.setText("BACK");
        jButton5.setBorderPainted(false);
        jButton5.setContentAreaFilled(false);
        jButton5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton5.setIconTextGap(-80);
        jButton5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton5.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonPressed.png"))); // NOI18N
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5backButtonActionPerformed(evt);
            }
        });
        addResident.add(jButton5);
        jButton5.setBounds(868, 188, 100, 60);

        jTextField10.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        addResident.add(jTextField10);
        jTextField10.setBounds(90, 270, 500, 30);

        addNewResidentButton.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        addNewResidentButton.setForeground(new java.awt.Color(255, 255, 255));
        addNewResidentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/longButtonIdle.png"))); // NOI18N
        addNewResidentButton.setText("ADD NEW RESIDENT");
        addNewResidentButton.setBorderPainted(false);
        addNewResidentButton.setContentAreaFilled(false);
        addNewResidentButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        addNewResidentButton.setIconTextGap(-230);
        addNewResidentButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        addNewResidentButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/longButtonPressed.png"))); // NOI18N
        addResident.add(addNewResidentButton);
        addNewResidentButton.setBounds(90, 730, 250, 60);

        jComboBox1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female" }));
        addResident.add(jComboBox1);
        jComboBox1.setBounds(90, 390, 90, 30);

        jTextField11.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        addResident.add(jTextField11);
        jTextField11.setBounds(90, 500, 500, 30);

        jComboBox2.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Yes", "No" }));
        addResident.add(jComboBox2);
        jComboBox2.setBounds(90, 640, 90, 30);

        jLabel12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/residentForm.png"))); // NOI18N
        addResident.add(jLabel12);
        jLabel12.setBounds(0, 0, 1000, 900);

        jTabbedPane1.addTab("tab9", addResident);

        removeResident.setLayout(null);

        jButton6.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonIdle.png"))); // NOI18N
        jButton6.setText("BACK");
        jButton6.setBorderPainted(false);
        jButton6.setContentAreaFilled(false);
        jButton6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton6.setIconTextGap(-80);
        jButton6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton6.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttons/shortButtonPressed.png"))); // NOI18N
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6backButtonActionPerformed(evt);
            }
        });
        removeResident.add(jButton6);
        jButton6.setBounds(868, 188, 100, 60);

        residentsTable.setBackground(new java.awt.Color(96, 108, 56));
        residentsTable.setFont(new java.awt.Font("Arial Black", 0, 12)); // NOI18N
        residentsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Sex", "Age", "Registered Voter"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane2.setViewportView(residentsTable);

        removeResident.add(jScrollPane2);
        jScrollPane2.setBounds(180, 200, 650, 500);

        removeResidentButton.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        removeResidentButton.setForeground(new java.awt.Color(255, 255, 255));
        removeResidentButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonIdle.png"))); // NOI18N
        removeResidentButton.setText("REMOVE");
        removeResidentButton.setBorderPainted(false);
        removeResidentButton.setContentAreaFilled(false);
        removeResidentButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        removeResidentButton.setIconTextGap(-120);
        removeResidentButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        removeResidentButton.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonPressed.png"))); // NOI18N
        removeResident.add(removeResidentButton);
        removeResidentButton.setBounds(425, 770, 150, 60);

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/residents.png"))); // NOI18N
        removeResident.add(jLabel13);
        jLabel13.setBounds(0, 0, 1000, 900);

        jTabbedPane1.addTab("tab10", removeResident);

        reservations.setLayout(null);

        acceptedReservationList.setBackground(new java.awt.Color(96, 108, 56));
        acceptedReservationList.setFont(new java.awt.Font("Arial Black", 0, 12)); // NOI18N
        acceptedReservationList.setForeground(new java.awt.Color(255, 255, 255));
        acceptedReservationList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Date", "Time", "Reference No."
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        acceptedReservationList.setFocusable(false);
        acceptedReservationList.setGridColor(new java.awt.Color(255, 255, 255));
        acceptedReservationList.setRowSelectionAllowed(false);
        acceptedReservationList.setSelectionBackground(new java.awt.Color(64, 74, 36));
        acceptedReservationList.setSelectionForeground(new java.awt.Color(255, 255, 255));
        jScrollPane4.setViewportView(acceptedReservationList);

        reservations.add(jScrollPane4);
        jScrollPane4.setBounds(60, 220, 880, 250);

        reservationList.setBackground(new java.awt.Color(96, 108, 56));
        reservationList.setFont(new java.awt.Font("Arial Black", 0, 12)); // NOI18N
        reservationList.setForeground(new java.awt.Color(255, 255, 255));
        reservationList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Date", "Time", "Reference No."
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        reservationList.setGridColor(new java.awt.Color(255, 255, 255));
        reservationList.setSelectionBackground(new java.awt.Color(64, 74, 36));
        reservationList.setSelectionForeground(new java.awt.Color(255, 255, 255));
        jScrollPane3.setViewportView(reservationList);

        reservations.add(jScrollPane3);
        jScrollPane3.setBounds(60, 520, 880, 250);

        rejectReservationRequest.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        rejectReservationRequest.setForeground(new java.awt.Color(255, 255, 255));
        rejectReservationRequest.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonIdle.png"))); // NOI18N
        rejectReservationRequest.setText("REJECT");
        rejectReservationRequest.setBorderPainted(false);
        rejectReservationRequest.setContentAreaFilled(false);
        rejectReservationRequest.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        rejectReservationRequest.setIconTextGap(-115);
        rejectReservationRequest.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rejectReservationRequest.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonPressed.png"))); // NOI18N
        rejectReservationRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rejectReservationRequestActionPerformed(evt);
            }
        });
        reservations.add(rejectReservationRequest);
        rejectReservationRequest.setBounds(230, 800, 150, 60);

        acceptReservationRequest.setFont(new java.awt.Font("Arial Black", 1, 18)); // NOI18N
        acceptReservationRequest.setForeground(new java.awt.Color(255, 255, 255));
        acceptReservationRequest.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonIdle.png"))); // NOI18N
        acceptReservationRequest.setText("ACCEPT");
        acceptReservationRequest.setBorderPainted(false);
        acceptReservationRequest.setContentAreaFilled(false);
        acceptReservationRequest.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        acceptReservationRequest.setIconTextGap(-115);
        acceptReservationRequest.setMargin(new java.awt.Insets(0, 0, 0, 0));
        acceptReservationRequest.setPressedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buttonPressed.png"))); // NOI18N
        acceptReservationRequest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptReservationRequestActionPerformed(evt);
            }
        });
        reservations.add(acceptReservationRequest);
        acceptReservationRequest.setBounds(630, 800, 150, 60);

        jLabel15.setBackground(new java.awt.Color(40, 54, 24));
        jLabel15.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(40, 54, 24));
        jLabel15.setText("ACCEPTED BOOKING");
        reservations.add(jLabel15);
        jLabel15.setBounds(405, 190, 190, 22);

        jLabel16.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(40, 54, 24));
        jLabel16.setText("PENDING REQUESTS");
        reservations.add(jLabel16);
        jLabel16.setBounds(405, 490, 190, 22);

        jLabel14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/reservations.png"))); // NOI18N
        jLabel14.setText("jLabel14");
        reservations.add(jLabel14);
        jLabel14.setBounds(0, 0, 1000, 900);

        jTabbedPane1.addTab("tab11", reservations);

        jTabbedPane1.setSelectedIndex(6);

        getContentPane().add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(198, -30, 1000, 930));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    Timer t;

    public void curDateTime() {
        t = new Timer(0, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                dateTime.setText(dtf.format(now));

            }
        });
        t.start();
    }

    private void accountsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_accountsButtonActionPerformed
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_accountsButtonActionPerformed

    private void complaintsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_complaintsButtonActionPerformed

    }//GEN-LAST:event_complaintsButtonActionPerformed

    private void updateAccountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateAccountButtonActionPerformed
        jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event_updateAccountButtonActionPerformed

    private void createAccountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createAccountButtonActionPerformed
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_createAccountButtonActionPerformed

    private void deleteAccountButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAccountButtonActionPerformed
        jTabbedPane1.setSelectedIndex(5);
        loadAccountsIntoTable();
    }//GEN-LAST:event_deleteAccountButtonActionPerformed

    private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_backButtonActionPerformed

    private void changeUsername1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeUsername1ActionPerformed
        jTabbedPane1.setSelectedIndex(3);
    }//GEN-LAST:event_changeUsername1ActionPerformed

    private void changePassword1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePassword1ActionPerformed
        jTabbedPane1.setSelectedIndex(4);
    }//GEN-LAST:event_changePassword1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        jTabbedPane1.setSelectedIndex(2);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void logOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logOutButtonActionPerformed
        new Login().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_logOutButtonActionPerformed

    private void CreateAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CreateAccountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_CreateAccountActionPerformed

    private void changeUsernameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeUsernameButtonActionPerformed
        String oldUsername = jTextField4.getText().trim();
        String newUsername = jTextField5.getText().trim();
        String enteredPassword = jTextField6.getText().trim();

        if (oldUsername.equals(newUsername)) {
            JOptionPane.showMessageDialog(null, "The new username cannot be the same as the old username.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String usernameRegex = "^[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(usernameRegex);
        Matcher matcher = pattern.matcher(newUsername);

        if (!oldUsername.isEmpty() && !newUsername.isEmpty()) {
            if (enteredPassword.equals(Home.getLoggedInUserPassword())) {
                if (matcher.matches()) {
                    boolean isUpdated = updateUsernameInFile("user.txt", oldUsername, newUsername)
                            || updateUsernameInFile("admin.txt", oldUsername, newUsername);

                    if (isUpdated) {
                        JOptionPane.showMessageDialog(null, "Username successfully updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        jTextField4.setText("");
                        jTextField5.setText("");
                        jTextField6.setText("");
                    } else {
                        JOptionPane.showMessageDialog(null, "The username does not exist or could not be updated.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "The username must be at least 2 characters long and contain only letters.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Invalid password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Username or password fields cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_changeUsernameButtonActionPerformed

    private void changePasswordButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePasswordButtonActionPerformed
        String username = jTextField7.getText().trim();
        String oldPassword = jTextField8.getText().trim();
        String newPassword = jTextField9.getText().trim();

        if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (newPassword.length() < 6) { // Changed this line
            JOptionPane.showMessageDialog(null, "The new password must be at least 6 characters long.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isUpdated = updatePasswordInFile("user.txt", username, oldPassword, newPassword)
                || updatePasswordInFile("admin.txt", username, oldPassword, newPassword);

        if (isUpdated) {
            JOptionPane.showMessageDialog(null, "Password successfully updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
            jTextField7.setText("");
            jTextField8.setText("");
            jTextField9.setText("");
        } else {
            JOptionPane.showMessageDialog(null, "The username does not exist or the old password is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_changePasswordButtonActionPerformed

    private void homeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homeButtonActionPerformed
        jTabbedPane1.setSelectedIndex(6);
    }//GEN-LAST:event_homeButtonActionPerformed

    private void DeleteButtonbackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeleteButtonbackButtonActionPerformed
        DeleteButton = new JButton("Delete Selected");
        DeleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                deleteSelectedAccounts();
            }
        });
    }//GEN-LAST:event_DeleteButtonbackButtonActionPerformed

    private void residentsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_residentsButtonActionPerformed
        jTabbedPane1.setSelectedIndex(7);
    }//GEN-LAST:event_residentsButtonActionPerformed

    private void addNewResiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewResiActionPerformed
        jTabbedPane1.setSelectedIndex(8);
    }//GEN-LAST:event_addNewResiActionPerformed

    private void updateResiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateResiActionPerformed
        jTabbedPane1.setSelectedIndex(9);
    }//GEN-LAST:event_updateResiActionPerformed

    private void jButton5backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5backButtonActionPerformed
        jTabbedPane1.setSelectedIndex(7);
    }//GEN-LAST:event_jButton5backButtonActionPerformed

    private void jButton6backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6backButtonActionPerformed
        jTabbedPane1.setSelectedIndex(7);
    }//GEN-LAST:event_jButton6backButtonActionPerformed

    private void reservationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reservationButtonActionPerformed
        jTabbedPane1.setSelectedIndex(10);
    }//GEN-LAST:event_reservationButtonActionPerformed

    private void rejectReservationRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rejectReservationRequestActionPerformed
        DefaultTableModel model = (DefaultTableModel) reservationList.getModel();

        int selectedRow = reservationList.getSelectedRow();
        int selectedColumn = reservationList.getSelectedColumn();

        // Check if a cell is selected
        if (selectedRow != -1 && selectedColumn != -1) {
            Object name = reservationList.getValueAt(selectedRow, 0);
            Object date = reservationList.getValueAt(selectedRow, 1);
            Object time = reservationList.getValueAt(selectedRow, 2);
            Object refNum = reservationList.getValueAt(selectedRow, 3);

            deleteReservationRequest(name.toString(), date.toString(), time.toString(), refNum.toString(), "reservationsRequests.txt");
            model.removeRow(selectedRow);
        }
    }//GEN-LAST:event_rejectReservationRequestActionPerformed

    private void acceptReservationRequestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptReservationRequestActionPerformed
        DefaultTableModel model = (DefaultTableModel) reservationList.getModel();
        DefaultTableModel model2 = (DefaultTableModel) acceptedReservationList.getModel();

        int selectedRow = reservationList.getSelectedRow();
        int selectedColumn = reservationList.getSelectedColumn();

        // Check if a cell is selected
        if (selectedRow != -1 && selectedColumn != -1) {
            Object name = reservationList.getValueAt(selectedRow, 0);
            Object date = reservationList.getValueAt(selectedRow, 1);
            Object time = reservationList.getValueAt(selectedRow, 2);
            Object refNum = reservationList.getValueAt(selectedRow, 3);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("reservations.txt", true))) {
                writer.write(name + "," + date + "," + time + "," + refNum);
                writer.newLine();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            deleteReservationRequest(name.toString(), date.toString(), time.toString(), refNum.toString(), "reservationsRequests.txt");
            model.removeRow(selectedRow);
            String[] acceptedData = {name.toString(), date.toString(), time.toString(), refNum.toString()};
            model2.insertRow(0, acceptedData);

        }
    }//GEN-LAST:event_acceptReservationRequestActionPerformed

    //reservations eme
    private List<String[]> readReservationsInReverseOrder(String filename) {
        List<String[]> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                rows.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.reverse(rows);
        return rows;
    }

    private void updateTable() {
        List<String[]> rows = readReservationsInReverseOrder("reservationsRequests.txt");
        for (String[] row : rows) {
            ((javax.swing.table.DefaultTableModel) reservationList.getModel()).addRow(row);
        }

        List<String[]> rows1 = readReservationsInReverseOrder("reservations.txt");
        for (String[] row : rows1) {
            ((javax.swing.table.DefaultTableModel) acceptedReservationList.getModel()).addRow(row);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Home.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Home.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Home.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Home.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Home().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CreateAccount;
    private javax.swing.JButton DeleteButton;
    private javax.swing.JPanel SideBar;
    private javax.swing.JButton acceptReservationRequest;
    private javax.swing.JTable acceptedReservationList;
    private javax.swing.JButton accountsButton;
    private javax.swing.JPanel accountsPanel;
    private javax.swing.JButton addNewResi;
    private javax.swing.JButton addNewResidentButton;
    private javax.swing.JPanel addResident;
    private javax.swing.JButton backButton;
    private javax.swing.JPanel changePassword;
    private javax.swing.JButton changePassword1;
    private javax.swing.JButton changePasswordButton;
    private javax.swing.JPanel changeUsername;
    private javax.swing.JButton changeUsername1;
    private javax.swing.JButton changeUsernameButton;
    private javax.swing.JButton clearanceButton;
    private javax.swing.JButton complaintsButton;
    private javax.swing.JPanel createAccount;
    private javax.swing.JButton createAccountButton;
    private javax.swing.JLabel dateTime;
    private javax.swing.JPanel delete;
    private javax.swing.JButton deleteAccountButton;
    private javax.swing.JButton homeButton;
    private javax.swing.JButton idCardButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JButton logOutButton;
    private javax.swing.JLabel registeredVotersCounter;
    private javax.swing.JButton rejectReservationRequest;
    private javax.swing.JPanel removeResident;
    private javax.swing.JButton removeResidentButton;
    private javax.swing.JButton reservationButton;
    private javax.swing.JTable reservationList;
    private javax.swing.JPanel reservations;
    private javax.swing.JPanel residents;
    private javax.swing.JButton residentsButton;
    private javax.swing.JTable residentsTable;
    private javax.swing.JLabel totalResidentCounter;
    private javax.swing.JPanel update;
    private javax.swing.JButton updateAccountButton;
    private javax.swing.JButton updateResi;
    private javax.swing.JComboBox<String> userType;
    private javax.swing.JPanel welcomeAdmin;
    // End of variables declaration//GEN-END:variables
}
