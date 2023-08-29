package application;

import java.awt.Button;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.Window;

public class Controller implements Initializable {
	@FXML
	private Pane pane;
	@FXML
	private Label mediaName, startTime, endTime;
	@FXML
	private Button prevButton, prevButton1, close;

	@FXML
	private ToggleButton playPauseButton;
	@FXML
	private ComboBox<String> speedBox;
	@FXML
	private Slider soundBar, processBar;
	@FXML
	private ImageView image;
	@FXML
	private Stage stage;
	private Media media;
	private MediaPlayer mediaPlayer;
	private File directory;
	private File[] files;
	private ArrayList<File> songs;
	private int songNumber;
	private float[] speeds = { (float) .25, (float) .5, (float) .75, 1, (float) 1.25, (float) 1.5, (float) 1.75, 2 };
	private Timer timer;
	private TimerTask task;
	private boolean runing;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		String fileName="playList.txt";
		createFile(fileName);

		soundBar.setOnMousePressed(null);

		songs = new ArrayList<File>();

		directory = new File("E:\\Music\\SnapTube Audio");

		files = directory.listFiles();

		if (files != null) {
			for (File file : files) {
				songs.add(file);
//				System.out.println(file);
			}
		}
		
		fileWrite(fileName, songs);
		
		ArrayList<File> playList=fileRead(fileName);
		
		System.out.println("playList: "+playList);
		
		media = new Media(songs.get(songNumber).toURI().toString());
		mediaPlayer = new MediaPlayer(media);
		mediaName.setText(songs.get(songNumber).getName());

		for (int i = 0; i < speeds.length; i++) {
			speedBox.getItems().add(Float.toString(speeds[i]) + "x");
		}
//		speedBox.setOnAction(this::changeSpeed);

		media.getMetadata().addListener(new MapChangeListener<String, Object>() {
			@Override
			public void onChanged(Change<? extends String, ? extends Object> ch) {
				if (ch.wasAdded()) {
					handleMetadata(ch.getKey(), ch.getValueAdded());
//	            System.out.println(ch.getKey() + " " + ch.getValueAdded());
				}
			}
		});

		soundBar.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				mediaPlayer.setVolume(soundBar.getValue() * .01);

			}

		});

		mediaPlayer.setOnReady(() -> {
			int totalDuration = (int) media.getDuration().toSeconds();
			processBar.setMax(totalDuration);
			endTime.setText(formatTime(totalDuration));
		});

		processBar.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (processBar.isValueChanging()) {
				mediaPlayer.seek(
						mediaPlayer.getMedia().getDuration().multiply(newValue.doubleValue() / processBar.getMax()));
			}
//			double per=100.0 * newValue.doubleValue() / processBar.getMax();
//			String style= "-fx-background-color: red";
//			processBar.setStyle(style);
		});

		mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
			if (!processBar.isValueChanging()) {
				int currentTime = (int) newValue.toSeconds();
				processBar.setValue(currentTime);
				startTime.setText(formatTime(currentTime));
			}
		});

		mediaPlayer.setOnEndOfMedia(() -> {
			playNextSong();
		});
		playMedia();

//		playPauseButton.onMouseEnteredProperty();

	}

	private void playNextSong() {
		nextMedia();

	}

	private void handleMetadata(String key, Object value) {
//		    if (key.equals("album")) {
//		      album.setText(value.toString());
//		    } else if (key.equals("artist")) {
//		      artist.setText(value.toString());
//		    } if (key.equals("title")) {
//		      title.setText(value.toString());
//		    } if (key.equals("year")) {
//		      year.setText(value.toString());
//		    } 
		if (key.equals("image")) {
//		    	System.out.println(value);
			Image img = (Image) value;
			if (img != null) {
				System.out.println(img);
				image.setImage(img);
			} else {
				Image img2 = null;
				try {
					img2 = new Image(new FileInputStream("mp3.png"));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				image.setImage(img2);
			}
		}
	}

	private String formatTime(int seconds) {
		int minutes = seconds / 60;
//		int hour=minutes/60;
//		int remainingminutes = seconds / 60;
		int remainingSeconds = seconds % 60;
		return String.format("%02d:%02d", minutes, remainingSeconds);
	}

	public void playMedia() {
		changeSpeed();
		mediaPlayer.play();
	}

	public void pauseMedia() {

	}

	public void nextMedia() {
		if (songNumber < songs.size() - 1) {
			songNumber++;
			mediaPlayer.stop();
			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			mediaName.setText(songs.get(songNumber).getName());

			mediaPlayer.setOnReady(() -> {
				int totalDuration = (int) media.getDuration().toSeconds();
				processBar.setMax(totalDuration);
				endTime.setText(formatTime(totalDuration));
			});

			processBar.valueProperty().addListener((observable, oldValue, newValue) -> {
				if (processBar.isValueChanging()) {
					mediaPlayer.seek(mediaPlayer.getMedia().getDuration()
							.multiply(newValue.doubleValue() / processBar.getMax()));
				}
			});

			mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
				if (!processBar.isValueChanging()) {
					int currentTime = (int) newValue.toSeconds();
					processBar.setValue(currentTime);
					startTime.setText(formatTime(currentTime));
				}
			});
			mediaPlayer.setOnEndOfMedia(() -> {
				playNextSong();
			});

			playMedia();
		} else {
			songNumber = 0;
			mediaPlayer.stop();
			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			mediaName.setText(songs.get(songNumber).getName());

			mediaPlayer.setOnReady(() -> {
				int totalDuration = (int) media.getDuration().toSeconds();
				processBar.setMax(totalDuration);
				endTime.setText(formatTime(totalDuration));
			});

			processBar.valueProperty().addListener((observable, oldValue, newValue) -> {
				if (processBar.isValueChanging()) {
					mediaPlayer.seek(mediaPlayer.getMedia().getDuration()
							.multiply(newValue.doubleValue() / processBar.getMax()));
				}
			});

			mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
				if (!processBar.isValueChanging()) {
					int currentTime = (int) newValue.toSeconds();
					processBar.setValue(currentTime);
					startTime.setText(formatTime(currentTime));
				}
			});
			mediaPlayer.setOnEndOfMedia(() -> {
				playNextSong();
			});

			playMedia();
		}
	}

	public void previousMedia() {
		if (songNumber == 0) {
			songNumber = songs.size() - 1;
			mediaPlayer.stop();
			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			mediaName.setText(songs.get(songNumber).getName());

			mediaPlayer.setOnReady(() -> {
				int totalDuration = (int) media.getDuration().toSeconds();
				processBar.setMax(totalDuration);
				endTime.setText(formatTime(totalDuration));
			});

			processBar.valueProperty().addListener((observable, oldValue, newValue) -> {
				if (processBar.isValueChanging()) {
					mediaPlayer.seek(mediaPlayer.getMedia().getDuration()
							.multiply(newValue.doubleValue() / processBar.getMax()));
				}
			});

			mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
				if (!processBar.isValueChanging()) {
					int currentTime = (int) newValue.toSeconds();
					processBar.setValue(currentTime);
					startTime.setText(formatTime(currentTime));
				}
			});
			mediaPlayer.setOnEndOfMedia(() -> {
				playNextSong();
			});

			playMedia();
		} else {
			songNumber--;
			mediaPlayer.stop();
			media = new Media(songs.get(songNumber).toURI().toString());
			mediaPlayer = new MediaPlayer(media);
			mediaName.setText(songs.get(songNumber).getName());

			mediaPlayer.setOnReady(() -> {
				int totalDuration = (int) media.getDuration().toSeconds();
				processBar.setMax(totalDuration);
				endTime.setText(formatTime(totalDuration));
			});

			processBar.valueProperty().addListener((observable, oldValue, newValue) -> {
				if (processBar.isValueChanging()) {
					mediaPlayer.seek(mediaPlayer.getMedia().getDuration()
							.multiply(newValue.doubleValue() / processBar.getMax()));
				}
			});

			mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
				if (!processBar.isValueChanging()) {
					int currentTime = (int) newValue.toSeconds();
					processBar.setValue(currentTime);
					startTime.setText(formatTime(currentTime));
				}
			});
			mediaPlayer.setOnEndOfMedia(() -> {
				playNextSong();
			});

			playMedia();
		}
	}

	public void closeMedia() {
		System.exit(0);
	}

	public void miniMedia() {
		stage = (Stage) Window.getWindows();
		stage.setIconified(true);
	}

	public void beginTimer() {

	}

	public void cancelTimer() {

	}

	public void changeSpeed() {
		if (speedBox.getValue() == null) {
			mediaPlayer.setRate(1);
		} else {
			mediaPlayer.setRate(Double.parseDouble(speedBox.getValue().substring(0, speedBox.getValue().length() - 1)));
		}
	}

	public void playPauseMedia() {
		if (playPauseButton.isSelected() || mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
			mediaPlayer.pause();
		} else {
			changeSpeed();
			mediaPlayer.play();
		}
	}
	
	public void fileWrite(String fileName, ArrayList<File> arrayList) {
		

        // Write the ArrayList to the text file
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            for (File item : arrayList) {
                writer.write(item.getAbsolutePath());
                writer.newLine();
            }
            writer.close();
            System.out.println("ArrayList saved to " + fileName);
        } catch (IOException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
	}
	
	public ArrayList<File> fileRead(String inputPath) {
        ArrayList<File> arrayList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                File file = new File(line);
                arrayList.add(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayList;
    }
	
	public void createFile(String fileName) {
		try {
            Path path = Paths.get(fileName);
            Files.createFile(path);
            System.out.println("File created successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
}

