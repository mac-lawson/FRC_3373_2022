// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.SparkMaxRelativeEncoder;
import com.revrobotics.CANSparkMax.ControlType;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.motors.MotorFactory;
import frc.robot.subsystems.ClimberSubsystem;
import frc.robot.subsystems.IntakeSubsystem;
import frc.robot.subsystems.ShooterSubsystem;

import edu.wpi.first.wpilibj.Timer;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  public static final int DRIVER_PORT = 0;
  public static final int SPECIAL_OPS_PORT = 1;
  public static final int INTAKE_PORT = 9;
  public static final int SHOOTER_LAUNCH_PORT = 10;
  public static final int SHOOTER_INDEXER_PORT = 11;
  public static final int SHOOTER_SWITCH_PORT = 1;
  public static final int CLIMBER_EXTENDER_PORT = 12;
  public static final int CLIMBER_ROTATOR_PORT = 13;
  public static final int CLIMBER_EXTENDER_SWITCH = 3;
  public static final int CLIMBER_ROTATOR_SWITCH = 4;
  public static final int FRONT_LEFT_ANGLE_ID = 8;
  public static final int FRONT_LEFT_DRIVE_ID = 7;
  public static final int FRONT_RIGHT_ANGLE_ID = 6;
  public static final int FRONT_RIGHT_DRIVE_ID = 5;
  public static final int BACK_RIGHT_ANGLE_ID = 4;
  public static final int BACK_RIGHT_DRIVE_ID = 3;
  public static final int BACK_LEFT_ANGLE_ID = 2;
  public static final int BACK_LEFT_DRIVE_ID = 1;

  public static final boolean USE_CAMERAS = false;
  public static final int FRONT_CAMERA_PORT = 0;
  public static final int BACK_CAMERA_PORT = 1;

  // SWERVE CONSTANTS
  public static double MaxSpeed = 0.3;
  public static double MaxRotation = 5;
  public static double RotationLimit = 3;
  public static double StrafeLimit = .25;
  public static double MagicRotateAngle = 2.72;

  private XboxController specialops;
  private IntakeSubsystem intake;
  private ShooterSubsystem shooter;
  private ClimberSubsystem climber;
  private CANSparkMax frontLeftAngleMotor;
  private CANSparkMax frontLeftDriveMotor;
  private CANSparkMax frontRightDriveMotor;
  private CANSparkMax frontRightAngleMotor;
  private CANSparkMax backLeftAngleMotor;
  private CANSparkMax backLeftDriveMotor;
  private CANSparkMax backRightDriveMotor;
  private CANSparkMax backRightAngleMotor;
  private SparkMaxPIDController frontLeftPidController;
  private SparkMaxPIDController frontRightPidController;
  private SparkMaxPIDController backRightPidController;
  private SparkMaxPIDController backLeftPidController;
  private RelativeEncoder frontLeftAngleEncoder;
  private RelativeEncoder frontRightAngleEncoder;
  private RelativeEncoder backRightAngleEncoder;
  private RelativeEncoder backLeftAngleEncoder;
  private XboxController drive_control;
  private Timer autotimer;
  private double turboFactor;
  private double reverseFactor;
  private double kP, kI, kD, kIz, kFF, kMaxOutput, kMinOutput;
  private double autonomousStart;

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    SmartDashboard.putString("MotorTesting: ", "None");
    drive_control = new XboxController(DRIVER_PORT);
    intake = new IntakeSubsystem(drive_control, INTAKE_PORT);
    
    specialops = new XboxController(SPECIAL_OPS_PORT);
    shooter = new ShooterSubsystem(specialops, SHOOTER_LAUNCH_PORT, SHOOTER_INDEXER_PORT, SHOOTER_SWITCH_PORT);
    climber = new ClimberSubsystem(specialops, CLIMBER_EXTENDER_PORT, CLIMBER_EXTENDER_SWITCH, CLIMBER_ROTATOR_PORT, CLIMBER_ROTATOR_SWITCH);

    frontLeftAngleMotor = new CANSparkMax(FRONT_LEFT_ANGLE_ID, MotorType.kBrushed);
    frontLeftDriveMotor = new CANSparkMax(FRONT_LEFT_DRIVE_ID, MotorType.kBrushless);
    frontRightAngleMotor = new CANSparkMax(FRONT_RIGHT_ANGLE_ID, MotorType.kBrushed);
    frontRightDriveMotor = new CANSparkMax(FRONT_RIGHT_DRIVE_ID, MotorType.kBrushless);
    backRightAngleMotor = new CANSparkMax(BACK_RIGHT_ANGLE_ID, MotorType.kBrushed);
    backRightDriveMotor = new CANSparkMax(BACK_RIGHT_DRIVE_ID, MotorType.kBrushless);
    backLeftDriveMotor = new CANSparkMax(BACK_LEFT_DRIVE_ID, MotorType.kBrushless);
    backLeftAngleMotor = new CANSparkMax(BACK_LEFT_ANGLE_ID, MotorType.kBrushed);

    frontLeftPidController = frontLeftAngleMotor.getPIDController();
    frontRightPidController = frontRightAngleMotor.getPIDController();
    backRightPidController = backRightAngleMotor.getPIDController();
    backLeftPidController = backLeftAngleMotor.getPIDController();

    // RESET SPARK MAX
    frontLeftAngleMotor.restoreFactoryDefaults();
    frontRightAngleMotor.restoreFactoryDefaults();
    frontLeftDriveMotor.restoreFactoryDefaults();
    frontRightDriveMotor.restoreFactoryDefaults();
    backLeftAngleMotor.restoreFactoryDefaults();
    backRightAngleMotor.restoreFactoryDefaults();
    backLeftDriveMotor.restoreFactoryDefaults();
    backRightDriveMotor.restoreFactoryDefaults();

    frontRightDriveMotor.setOpenLoopRampRate(1.0);
    frontLeftDriveMotor.setOpenLoopRampRate(1.0);
    backRightDriveMotor.setOpenLoopRampRate(1.0);
    backLeftDriveMotor.setOpenLoopRampRate(1.0);

    //TIMER
    autotimer = new Timer();

    frontLeftAngleEncoder = frontLeftAngleMotor.getEncoder(SparkMaxRelativeEncoder.Type.kQuadrature, 104);
    frontLeftAngleEncoder.setPosition(0);
    frontLeftAngleEncoder.setInverted(false);

    frontRightAngleEncoder = frontRightAngleMotor.getEncoder(SparkMaxRelativeEncoder.Type.kQuadrature, 104);
    frontRightAngleEncoder.setInverted(false);
    frontRightAngleEncoder.setPosition(0);

    backRightAngleEncoder = backRightAngleMotor.getEncoder(SparkMaxRelativeEncoder.Type.kQuadrature, 104);
    backRightAngleEncoder.setInverted(false);
    backRightAngleEncoder.setPosition(0);

    backLeftAngleEncoder = backLeftAngleMotor.getEncoder(SparkMaxRelativeEncoder.Type.kQuadrature, 104);
    backLeftAngleEncoder.setInverted(false);
    backLeftAngleEncoder.setPosition(0);

    turboFactor = 1.0;
    reverseFactor = 1.0;

    // kP = 75; 
    // kI = 1e-3;
    // kD = 1; 
    // kIz = 0; 
    // kFF = 0; 
    // kMaxOutput = 1; 
    // kMinOutput = -1;

    kP = 1; 
    kI = 1e-4;
    kD = 1; 
    kIz = 0; 
    kFF = 0; 
    kMaxOutput = 1; 
    kMinOutput = -1;

    frontLeftPidController.setFeedbackDevice(frontLeftAngleEncoder);
    frontRightPidController.setFeedbackDevice(frontRightAngleEncoder);
    backRightPidController.setFeedbackDevice(backRightAngleEncoder);
    backLeftPidController.setFeedbackDevice(backLeftAngleEncoder);

    frontLeftPidController.setP(kP);
    frontRightPidController.setP(kP);
    backRightPidController.setP(kP);
    backLeftPidController.setP(kP);

    frontLeftPidController.setI(kI);
    frontRightPidController.setI(kI);
    backRightPidController.setI(kI);
    backLeftPidController.setI(kI);

    frontLeftPidController.setD(kD);
    frontRightPidController.setD(kD);
    backRightPidController.setD(kD);
    backLeftPidController.setD(kD);

    frontLeftPidController.setIZone(kIz);
    frontRightPidController.setIZone(kIz);
    backRightPidController.setIZone(kIz);
    backLeftPidController.setIZone(kIz);

    frontLeftPidController.setFF(kFF);
    frontRightPidController.setFF(kFF);
    backRightPidController.setFF(kFF);
    backLeftPidController.setFF(kFF);

    frontLeftPidController.setOutputRange(kMinOutput, kMaxOutput);
    frontRightPidController.setOutputRange(kMinOutput, kMaxOutput);
    backRightPidController.setOutputRange(kMinOutput, kMaxOutput);
    backLeftPidController.setOutputRange(kMinOutput, kMaxOutput);

    
    frontLeftAngleMotor.setInverted(true);
    frontRightAngleMotor.setInverted(true);
    backLeftAngleMotor.setInverted(true);
    backRightAngleMotor.setInverted(true);

    if (USE_CAMERAS && !isSimulation()) {
      CameraServer.startAutomaticCapture("Front", FRONT_CAMERA_PORT);
      CameraServer.startAutomaticCapture("Back", BACK_CAMERA_PORT);  
    }

    autonomousStart = 0.0;
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    if (intake != null) {
      intake.robotPeriodic();
    }
    if (shooter != null) {
      shooter.robotPeriodic();
    }
    if (climber != null) {
      climber.robotPeriodic();
    }
    MotorFactory.updateDashboard();
    SmartDashboard.putNumber("FL Angle Position", frontLeftAngleEncoder.getPosition());
    SmartDashboard.putNumber("FR Angle Position", frontRightAngleEncoder.getPosition());
    SmartDashboard.putNumber("BR Angle Position", backRightAngleEncoder.getPosition());
    SmartDashboard.putNumber("BL Angle Position", backLeftAngleEncoder.getPosition());
    SmartDashboard.putBoolean("Drive Reversed?", reverseFactor < -1.0);
  }

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
    if (intake != null) {
      intake.autonomousInit();
    }
    if (shooter != null) {
      shooter.autonomousInit();
    }
    if (climber != null) {
      climber.autonomousInit();
    }
    autonomousStart = Timer.getFPGATimestamp();
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {

    if (intake != null) {
      intake.autonomousPeriodic();
    }
    if (shooter != null) {
     shooter.autonomousPeriodic();
    }
    if (climber != null) {
      climber.autonomousPeriodic();
    }

    double seconds = Timer.getFPGATimestamp() - autonomousStart;
    if (seconds < 0.3) {   // let the intake wheel wind up a bit
      forwardBy(0.0, 0.0);
    } else if (seconds < 0.5) {   // ooch forward (picks up speed)
      forwardBy(1.0, 0.0);
    } else if (seconds < 1.0) {   // ooch backwards (drops intake frame)
      forwardBy(-1.0, 0.0);
    } else if (seconds < 6.2) {  // wait in place for intake to drop
      forwardBy(0.0, 0.0);
    } else if (seconds < 9.0) {   // go get a ball
      forwardBy(0.1, -0.5);
    } else if (seconds < 9.3) {   // go get a ball
      forwardBy(0.1, 0.7);
    } else {
      forwardBy(0.0, 0.0);
    }

    /*
    if (autonomousStart > 0.0) {
      double seconds = Timer.getFPGATimestamp() - autonomousStart;
      if (seconds < 4) {
        double speed = 0.1;
        frontLeftDriveMotor.set(-speed);
        System.out.println("Running autorun at" + (frontLeftDriveMotor.get()));
        frontRightDriveMotor.set(speed);
        backLeftDriveMotor.set(-speed);
        backRightDriveMotor.set(speed);
        frontLeftPidController.setReference(0, CANSparkMax.ControlType.kPosition);
        frontRightPidController.setReference(0, CANSparkMax.ControlType.kPosition);
        backRightPidController.setReference(0, CANSparkMax.ControlType.kPosition);
        backLeftPidController.setReference(0, CANSparkMax.ControlType.kPosition);  
      } else {
        frontLeftDriveMotor.set(0);
        frontRightDriveMotor.set(0);
        backRightDriveMotor.set(0);  
        backLeftDriveMotor.set(0);
        autonomousStart = 0.0;
      }
    }
    */
  }

  private void forwardBy(double speed, double angle) {
    frontLeftPidController.setReference(0, CANSparkMax.ControlType.kPosition);
    frontRightPidController.setReference(0, CANSparkMax.ControlType.kPosition);
    backRightPidController.setReference(angle, CANSparkMax.ControlType.kPosition);
    backLeftPidController.setReference(angle, CANSparkMax.ControlType.kPosition);  
    frontLeftDriveMotor.set(-speed);
    frontRightDriveMotor.set(speed);
    backLeftDriveMotor.set(-speed);
    backRightDriveMotor.set(speed);
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {
  }

  @Override
  public void teleopPeriodic() {

    SmartDashboard.updateValues();

    if (intake != null) {
      intake.telopPeriodic();
    }
    if (shooter != null) {
      shooter.teleopPeriodic();      
    }
    if (climber != null) {
      climber.teleopPeriodic();
    }

    double rightX = drive_control.getRightX();
    double leftX = drive_control.getLeftX();
    double leftY = drive_control.getLeftY();

    // if someone hits start, we'll invert the "front" of the vehicle for driving
    if (drive_control.getStartButtonPressed()) {
      reverseFactor = reverseFactor * -1.0;
    }

    // if someone is holding the right trigger, we'll double speed
    if (drive_control.getRightTriggerAxis() > 0.5) {
      turboFactor = 2.0;
    } else {
      turboFactor = 1.0;
    }

    if (drive_control.getRightBumper()) {
      AimBot(rightX);
      return;
    }

    if (drive_control.getLeftBumper()) {
      macDrive(leftX, leftY, rightX);
      return;
    }
    
    // else if (Math.abs(rightX) > 0.05) {
    //   Rotate(rightX);
    //   return;
    // }
    
    driveDrive(leftX, leftY, rightX);

    // SnakeDrive();
    // StrafeSwerve();
    // ChangeLimited();
    //SnakeDrive();
  }

  /**
   * This is a "strafey" mode: the robot will move in the direction of the left stick,
   * without changing its heading. Two notes:
   * 
   *  - There's a discontinuity at full left and full right where the wheels will flip
   *  around.
   * 
   *  - Velocity is based on the distance of the joystick from the center, NOT just
   *  the forward angle of it.
   */
  public void macDrive(double leftX, double leftY, double rightX) {

    double moveSpeed = Math.sqrt(leftX * leftX + leftY * leftY) * MaxSpeed * turboFactor * reverseFactor;
    double turnAngle = leftX * leftX * leftX * MaxRotation * reverseFactor;    

    if (drive_control.getLeftY() > 0) {
      frontLeftDriveMotor.set(moveSpeed);
      frontRightDriveMotor.set(-moveSpeed);
      backRightDriveMotor.set(-moveSpeed);
      backLeftDriveMotor.set(moveSpeed);
      frontLeftPidController.setReference(turnAngle, CANSparkMax.ControlType.kPosition);
      frontRightPidController.setReference(turnAngle, CANSparkMax.ControlType.kPosition);
      backRightPidController.setReference(turnAngle, CANSparkMax.ControlType.kPosition);
      backLeftPidController.setReference(turnAngle, CANSparkMax.ControlType.kPosition);            
    }

    if (drive_control.getLeftY() < 0) {
      frontLeftDriveMotor.set(-moveSpeed);
      frontRightDriveMotor.set(moveSpeed);
      backRightDriveMotor.set(moveSpeed);
      backLeftDriveMotor.set(-moveSpeed);
      frontLeftPidController.setReference(turnAngle, CANSparkMax.ControlType.kPosition);
      frontRightPidController.setReference(turnAngle, CANSparkMax.ControlType.kPosition);
      backRightPidController.setReference(turnAngle, CANSparkMax.ControlType.kPosition);
      backLeftPidController.setReference(turnAngle, CANSparkMax.ControlType.kPosition);            
    }
  }

  /**
   * This is a "drivey" mode. The left stick controls forward/reverse speed, while
   * the right stick will turn you left or right like a car. There's a maximum
   * turn angle, to prevent the wheels from sticking.
   */
  public void driveDrive(double leftX, double leftY, double rightX) {

    double moveSpeed = Math.sqrt(leftX * leftX + leftY * leftY) * MaxSpeed * turboFactor * reverseFactor;
    double turnAngle = rightX * rightX * rightX * MaxRotation * reverseFactor;   
    if (turnAngle > RotationLimit) {
      turnAngle = RotationLimit;
    }

    if (drive_control.getLeftY() > 0) {
      frontLeftDriveMotor.set(moveSpeed);
      frontRightDriveMotor.set(-moveSpeed);
      backRightDriveMotor.set(-moveSpeed);
      backLeftDriveMotor.set(moveSpeed);
      frontLeftPidController.setReference(turnAngle, CANSparkMax.ControlType.kPosition);
      frontRightPidController.setReference(turnAngle, CANSparkMax.ControlType.kPosition);
      backRightPidController.setReference(-turnAngle, CANSparkMax.ControlType.kPosition);
      backLeftPidController.setReference(-turnAngle, CANSparkMax.ControlType.kPosition);            
    }

    if (drive_control.getLeftY() < 0) {
      frontLeftDriveMotor.set(-moveSpeed);
      frontRightDriveMotor.set(moveSpeed);
      backRightDriveMotor.set(moveSpeed);
      backLeftDriveMotor.set(-moveSpeed);
      frontLeftPidController.setReference(-turnAngle, CANSparkMax.ControlType.kPosition);
      frontRightPidController.setReference(-turnAngle, CANSparkMax.ControlType.kPosition);
      backRightPidController.setReference(turnAngle, CANSparkMax.ControlType.kPosition);
      backLeftPidController.setReference(turnAngle, CANSparkMax.ControlType.kPosition);            
    }
  }

  public void Rotate(double rightX) {
    double rotateSpeed = -rightX / 4;

    // rotation
    if (rightX > 0) {
      frontLeftDriveMotor.set(rotateSpeed);
      frontRightDriveMotor.set(rotateSpeed);
      backLeftDriveMotor.set(rotateSpeed);
      backRightDriveMotor.set(rotateSpeed);
      frontLeftPidController.setReference(-MagicRotateAngle,  CANSparkMax.ControlType.kPosition);
      frontRightPidController.setReference(MagicRotateAngle,  CANSparkMax.ControlType.kPosition);
      backRightPidController.setReference(-MagicRotateAngle,  CANSparkMax.ControlType.kPosition);
      backLeftPidController.setReference(MagicRotateAngle,  CANSparkMax.ControlType.kPosition);
    }
      // Rotation
    if (rightX < 0) {
      frontLeftDriveMotor.set(rotateSpeed);
      frontRightDriveMotor.set(rotateSpeed);
      backLeftDriveMotor.set(rotateSpeed);
      backRightDriveMotor.set(rotateSpeed);  
      frontLeftPidController.setReference(-MagicRotateAngle,  CANSparkMax.ControlType.kPosition);
      frontRightPidController.setReference(MagicRotateAngle,  CANSparkMax.ControlType.kPosition);
      backRightPidController.setReference(-MagicRotateAngle,  CANSparkMax.ControlType.kPosition);
      backLeftPidController.setReference(MagicRotateAngle,  CANSparkMax.ControlType.kPosition);
    }
  }
  public void StrafeSwerve() {
    if (drive_control.getRawAxis(0) > .5) {
      frontLeftPidController.setReference(4,  CANSparkMax.ControlType.kPosition);
      frontLeftDriveMotor.set(- StrafeLimit);
      frontRightPidController.setReference(4,  CANSparkMax.ControlType.kPosition);
      frontRightDriveMotor.set(- StrafeLimit);
      backRightPidController.setReference(4,  CANSparkMax.ControlType.kPosition);
      backLeftDriveMotor.set(StrafeLimit);
      backLeftPidController.setReference(4,  CANSparkMax.ControlType.kPosition);
      backRightDriveMotor.set(StrafeLimit);
    }
    if (drive_control.getRawAxis(0) < .5) {
      frontLeftPidController.setReference(4,  CANSparkMax.ControlType.kPosition);
      frontLeftDriveMotor.set(StrafeLimit);
      frontRightPidController.setReference(4,  CANSparkMax.ControlType.kPosition);
      frontRightDriveMotor.set(StrafeLimit);
      backRightPidController.setReference(4,  CANSparkMax.ControlType.kPosition);
      backLeftDriveMotor.set(- StrafeLimit);
      backLeftPidController.setReference(4,  CANSparkMax.ControlType.kPosition);
      backRightDriveMotor.set(- StrafeLimit);
    }    
  }

  public void AimBot(double rightX) {
    double rotateSpeed = -rightX / 8.0 * turboFactor;
    frontLeftDriveMotor.set(rotateSpeed);
    frontRightDriveMotor.set(rotateSpeed);
    backLeftDriveMotor.set(rotateSpeed);
    backRightDriveMotor.set(rotateSpeed);  
    frontLeftPidController.setReference(-MagicRotateAngle,  CANSparkMax.ControlType.kPosition);
    frontRightPidController.setReference(MagicRotateAngle,  CANSparkMax.ControlType.kPosition);
    backRightPidController.setReference(-MagicRotateAngle,  CANSparkMax.ControlType.kPosition);
    backLeftPidController.setReference(MagicRotateAngle,  CANSparkMax.ControlType.kPosition);
  }

  public void SnakeDrive() {
    boolean rt = drive_control.getRightTriggerAxis() > 0.05;
    boolean lt = drive_control.getLeftTriggerAxis() > 0.05;
    if(rt) {
      frontLeftPidController.setReference(2, ControlType.kPosition);
      frontRightPidController.setReference(2, ControlType.kPosition);
    }
    if(lt) {
      frontLeftPidController.setReference(-2, ControlType.kPosition);
      frontRightPidController.setReference(-2, ControlType.kPosition);
    }
    if (rt && lt) {
      frontLeftPidController.setReference(0, ControlType.kPosition);
      frontRightPidController.setReference(0, ControlType.kPosition);
    }
  }
  /*
  public void ChangeLimited() {
    if(drive_control.getRightBumper() == true) {
      DefaultLimit = 2;
      RotationLimit = .50;
      StrafeLimit = .50;
    }
    else {
      DefaultLimit = 4;
      RotationLimit = .25;
      StrafeLimit = .25; 
    }
  }
  */
  
  // /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
    if (intake != null) {
      intake.disabledInit();
    }
    if (shooter != null) {
      shooter.disabledInit();
    }
    if (climber != null) {
      climber.disabledInit();
    }
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {
    SmartDashboard.putNumber("P Gain", kP);
    SmartDashboard.putNumber("I Gain", kI);
    SmartDashboard.putNumber("D Gain", kD);
    SmartDashboard.putNumber("I Zone", kIz);
    SmartDashboard.putNumber("Feed Forward", kFF);
    SmartDashboard.putNumber("Max Output", kMaxOutput);
    SmartDashboard.putNumber("Min Output", kMinOutput);
    SmartDashboard.putNumber("Set Rotations", 0);


  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
    SmartDashboard.putString("MotorTesting: ", "None");
    if(drive_control.getBButton() == true) {
      frontLeftAngleEncoder.setPosition(0);
      System.out.print("Encoder 1 Reset");
      frontRightAngleEncoder.setPosition(0);
      System.out.print("Encoder 2 Reset");
      backRightAngleEncoder.setPosition(0);
      System.out.print("Encoder 3 Reset");
      backLeftAngleEncoder.setPosition(0);
      System.out.print("Encoder 4 Reset");
    }
    double p = SmartDashboard.getNumber("P Gain", 0);
    double i = SmartDashboard.getNumber("I Gain", 0);
    double d = SmartDashboard.getNumber("D Gain", 0);
    double iz = SmartDashboard.getNumber("I Zone", 0);
    double ff = SmartDashboard.getNumber("Feed Forward", 0);
    double max = SmartDashboard.getNumber("Max Output", 0);
    double min = SmartDashboard.getNumber("Min Output", 0);
    double rotations = SmartDashboard.getNumber("Set Rotations", 0);
    if((p != kP)) { frontLeftPidController.setP(p); kP = p; }
    if((i != kI)) { frontLeftPidController.setI(i); kI = i; }
    if((d != kD)) { frontLeftPidController.setD(d); kD = d; }
    if((iz != kIz)) { frontLeftPidController.setIZone(iz); kIz = iz; }
    if((ff != kFF)) { frontLeftPidController.setFF(ff); kFF = ff; }
    if((max != kMaxOutput) || (min != kMinOutput)) { 
      frontLeftPidController.setOutputRange(min, max); 
      kMinOutput = min; kMaxOutput = max; 
    }
    if((p != kP)) { frontRightPidController.setP(p); kP = p; }
    if((i != kI)) { frontRightPidController.setI(i); kI = i; }
    if((d != kD)) { frontRightPidController.setD(d); kD = d; }
    if((iz != kIz)) { frontRightPidController.setIZone(iz); kIz = iz; }
    if((ff != kFF)) { frontRightPidController.setFF(ff); kFF = ff; }
    if((max != kMaxOutput) || (min != kMinOutput)) { 
      frontRightPidController.setOutputRange(min, max); 
      kMinOutput = min; kMaxOutput = max; 
    }
    if((p != kP)) { backRightPidController.setP(p); kP = p; }
    if((i != kI)) { backRightPidController.setI(i); kI = i; }
    if((d != kD)) { backRightPidController.setD(d); kD = d; }
    if((iz != kIz)) { backRightPidController.setIZone(iz); kIz = iz; }
    if((ff != kFF)) { backRightPidController.setFF(ff); kFF = ff; }
    if((max != kMaxOutput) || (min != kMinOutput)) { 
      backRightPidController.setOutputRange(min, max); 
      kMinOutput = min; kMaxOutput = max; 
    }
    if((p != kP)) { backLeftPidController.setP(p); kP = p; }
    if((i != kI)) { backLeftPidController.setI(i); kI = i; }
    if((d != kD)) { backLeftPidController.setD(d); kD = d; }
    if((iz != kIz)) { backLeftPidController.setIZone(iz); kIz = iz; }
    if((ff != kFF)) { backLeftPidController.setFF(ff); kFF = ff; }
    if((max != kMaxOutput) || (min != kMinOutput)) { 
      backLeftPidController.setOutputRange(min, max); 
      kMinOutput = min; kMaxOutput = max; 
    }



    // MOTOR TEST CONTROLS


    while(drive_control.getAButton() == true) {
      SmartDashboard.putString("MotorTesting: ", "Front Left");
      frontLeftDriveMotor.set(drive_control.getLeftY());
      frontLeftAngleMotor.set(drive_control.getRightY());
    }
    while(drive_control.getBButton() == true) {
      SmartDashboard.putString("MotorTesting: ", "Front Right");
      frontRightDriveMotor.set(drive_control.getLeftY());
      frontRightAngleMotor.set(drive_control.getRightY());
    }
    while(drive_control.getXButton() == true) {   
       SmartDashboard.putString("MotorTesting: ", "Back Left");
      backLeftDriveMotor.set(drive_control.getLeftY());
      backLeftAngleMotor.set(drive_control.getRightY());
    }
    while(drive_control.getYButton() == true) {
      SmartDashboard.putString("MotorTesting: ", "Back Right");
      backRightDriveMotor.set(drive_control.getLeftY());
      backRightAngleMotor.set(drive_control.getRightY());
    }

  }
}