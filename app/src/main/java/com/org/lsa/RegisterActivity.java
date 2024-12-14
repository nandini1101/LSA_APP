package com.org.lsa;

import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.KeyStore;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class RegisterActivity extends AppCompatActivity {

    private static final String KEY_ALIAS = "MyStaticKeyAlias"; // Key alias used to store the static key
    private static final String STATIC_PASSWORD = "1234"; // Static password for registration
    private static final byte[] STATIC_KEY = "My12DigitKey12".getBytes(); // Your static 12-digit key as bytes

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Find views in layout
        usernameEditText = findViewById(R.id.et_username);
        passwordEditText = findViewById(R.id.et_password);
        registerButton = findViewById(R.id.btn_register);

        // Set up click listener for the register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Check if username and static password are correct
                if (!username.isEmpty() && password.equals(STATIC_PASSWORD)) {
                    try {
                        Log.d("RegisterActivity", "Username and password are valid, proceeding with key storage.");
                        deleteStaticKey();
                        storeStaticKey();
                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                        // Retrieve the key to check if it was stored successfully
                        String retrievedKey = retrieveStaticKey();
                        if (retrievedKey != null) {
                            Log.d("RegisterActivity", "Key retrieved successfully: " + retrievedKey);
                        } else {
                            Log.w("RegisterActivity", "Failed to retrieve the key.");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "Error storing key", Toast.LENGTH_SHORT).show();
                        Log.e("RegisterActivity", "Error occurred while storing the key: " + e.getMessage());
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    Log.w("RegisterActivity", "Invalid username or password");
                }
            }
        });
    }

    // Method to store the static key in the Keystore
    private void storeStaticKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null); // Load the Keystore

            // Check if the key alias exists
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                Log.d("RegisterActivity", "Key alias not found, storing static key.");

                // Create a KeyGenParameterSpec for the static key
                KeyGenParameterSpec keyGenParameterSpec = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                            KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setKeySize(128) // 128 bits
                            .build();
                }

                // Create a SecretKeyGenerator and generate the key
                KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    keyGenerator.init(keyGenParameterSpec);
                }
                SecretKey secretKey = keyGenerator.generateKey();

                Log.d("RegisterActivity", "Static key stored successfully.");
            } else {
                Log.d("RegisterActivity", "Key already exists, no need to store again.");
            }
        } catch (Exception e) {
            Log.e("RegisterActivity", "Error occurred while storing the key: " + e.getMessage());
        }
    }


    // Method to retrieve the static key from the Keystore
    public String retrieveStaticKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null); // Load the Keystore

            // Check if the alias exists and retrieve the key
            if (keyStore.containsAlias(KEY_ALIAS)) {
                KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
                SecretKey secretKey = secretKeyEntry.getSecretKey();

                // Convert secret key to a hex string for better representation
                byte[] keyBytes = secretKey.getEncoded();
                StringBuilder hexString = new StringBuilder();
                if (keyBytes != null) {
                    for (byte b : keyBytes) {
                        String hex = Integer.toHexString(0xFF & b);
                        if (hex.length() == 1) hexString.append('0');
                        hexString.append(hex);
                    }
                    Log.d("RegisterActivity", "Key retrieved successfully.");
                    return hexString.toString(); // Return the key as a hex string
                } else {
                    Log.w("RegisterActivity", "Key retrieval returned null.");
                }
            } else {
                Log.w("RegisterActivity", "Key alias not found.");
            }
        } catch (Exception e) {
            Log.e("RegisterActivity", "Error retrieving the key: " + e.getMessage());
        }

        return null; // Return null if retrieval fails
    }

    private void deleteStaticKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS);
                Log.d("RegisterActivity", "Key deleted successfully.");
            }
        } catch (Exception e) {
            Log.e("RegisterActivity", "Error deleting the key: " + e.getMessage());
        }
    }

}
