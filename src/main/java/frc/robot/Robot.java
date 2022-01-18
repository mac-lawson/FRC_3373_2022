
  
// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SerialPort;
import frc.robot.SwerveControl.DriveMode;
import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.wpilibj.DigitalInput;
import frc.robot.SuperJoystick;

import java.io.ObjectInputFilter.Status;
import java.lang.Thread;
import java.util.Scanner;
import java.util.concurrent.DelayQueue;
import com.kauailabs.navx.frc.AHRS;
// package for shoooter
import frc.robot.Shooter;
import edu.wpi.first.wpilibj.Timer;
// talons
//NOTE: not neccesary unless called in robot.java file
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import static com.revrobotics.CANSparkMax.ControlType;
import static com.revrobotics.SparkMaxAnalogSensor.Mode;
// end


/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  // declare driver and shooter joysticks
  private SuperJoystick driver;
  private SuperJoystick specialops;
  // declare navx for use during autonomous mode
  private SuperAHRS navx_old;
  // delcare shooting motor
  private CANSparkMax LargeMainWheel;
  private CANSparkMax SmallIndexerWheel;
  private CANSparkMax Intake;
  private DigitalInput indexer;
  private AHRS navx;
  //end
  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    LargeMainWheel = new CANSparkMax(1, MotorType.kBrushless);
    SmallIndexerWheel = new CANSparkMax(2, MotorType.kBrushless);
    Intake = new CANSparkMax(3, MotorType.kBrushless);
    driver = new SuperJoystick(0);
    // the two joysticks for both driers will be called speci
    specialops = new SuperJoystick(1);
    indexer = new DigitalInput(0);
    navx = new AHRS(SerialPort.Port.kUSB1);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {}

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {

  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    SmartDashboard.putNumber("Total Balls", frc.robot.Shooter.TotalBalls);
    SmartDashboard.putNumber("Maximum Shooting Speed", 1);
    SmartDashboard.updateValues();
    Double max_speed = .1;
    //start shooting wheel
    if (specialops.getTrigger()) {
      LargeMainWheel.set(max_speed);
    }
    else if (specialops.isStartPushed()) {
      max_speed += .1;
    }
    //lower shooting wheel speed
    else if (specialops.isBackPushed()) {
      max_speed -= .1;
    }
    //increase shooting wheel speed
    else if (indexer.get()) {
      frc.robot.Shooter.TotalBalls += 1;
    }
    //shoot
    else if (specialops.isAPushed()) {
      Timer delay = new Timer();
      delay.reset();
      delay.start();
      while (delay.get() < 1.1) {
        SmallIndexerWheel.set(0.05);
        frc.robot.Shooter.TotalBalls -= 1;
      }
      delay.stop();
    }
    // intake
    else if (specialops.getRawAxis(5) > 0.05) {      
      Timer accelerationDelay = new Timer();
      accelerationDelay.reset();
      accelerationDelay.start();
      while (accelerationDelay.get() < 1) {
        Intake.set(.1);
      }
      Intake.set(.2);
      accelerationDelay.stop();
    }
    /*
    A problem with the 2020 indexer code was the sensors could be accidentally tripped or think that
    the robot has more/less balls then it actually does. This allows for the ball number displayed in
    code to be reset along with the value send to smart dashboard. 
    */
    else if (driver.isBPushed()) {
      frc.robot.Shooter.TotalBalls = 0;
    }
    else if (max_speed > SmartDashboard.getNumber("Maximum Shooting Speed", 1)) {
      LargeMainWheel.set(0);
    }
    else {
      LargeMainWheel.set(0);
      SmallIndexerWheel.set(0);
      Intake.set(0);
      SmartDashboard.putNumber("Total Balls", frc.robot.Shooter.TotalBalls);
      SmartDashboard.updateValues();

    }


  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
    /** 
    String robotMode = "Debug"; 
    SmartDashboard.putString("Robot Mode", robotMode);
    SmartDashboard.updateValues();
    if (driver.get()) {
  }
*/

    }

}