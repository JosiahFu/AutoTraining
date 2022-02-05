// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.Button;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.POVButton;
import frc.robot.Constants.DriveTrain.DriveTrainNeutralMode;
import frc.robot.commands.auto.GroupThreeBallAuto;
import frc.robot.commands.auto.IndividualThreeBallAuto;
import frc.robot.commands.auto.OneBallAuto;
import frc.robot.commands.auto.TestPath;
import frc.robot.commands.auto.TwoBallAuto;
import frc.robot.commands.driveTrain.DriveForwardDistance;
import frc.robot.commands.driveTrain.SetArcadeDrive;
import frc.robot.commands.flywheel.SetRpmSetpoint;
import frc.robot.commands.led.GetSubsystemStates;
import frc.robot.simulation.FieldSim;
import frc.robot.subsystems.Climber;
import frc.robot.subsystems.Controls;
import frc.robot.subsystems.DriveTrain;
import frc.robot.subsystems.Flywheel;
import frc.robot.subsystems.Indexer;
import frc.robot.subsystems.Intake;
import frc.robot.subsystems.LED;
import frc.robot.subsystems.Turret;
import frc.robot.subsystems.Vision;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  private final DriveTrain m_driveTrain = new DriveTrain();
  private final Controls m_controls = new Controls();
  private final Turret m_turret = new Turret(m_driveTrain);
  private final Vision m_vision = new Vision(m_controls);
  private final Flywheel m_flywheel = new Flywheel(m_vision);
  private final Intake m_intake = new Intake();
  private final Indexer m_indexer = new Indexer();
  private final LED m_led = new LED();
  private final Climber m_climber = new Climber();

  private final FieldSim m_fieldSim = new FieldSim(m_driveTrain, m_intake);

  static Joystick leftJoystick = new Joystick(Constants.USB.leftJoystick);
  static Joystick rightJoystick = new Joystick(Constants.USB.rightJoystick);
  static XboxController xBoxController = new XboxController(Constants.USB.xBoxController);

  public Button[] leftButtons = new Button[2];
  public Button[] rightButtons = new Button[2];
  public Button[] xBoxButtons = new Button[10];
  public Button[] xBoxPOVButtons = new Button[8];
  public Button xBoxLeftTrigger, xBoxRightTrigger;
  // public static boolean allianceColorBlue;
  // public static boolean allianceColorRed;

  public static enum CommandSelector {
    BLUE_ALLIANCE, // 01
    RED_ALLIANCE
  }

  public final SendableChooser<CommandSelector> m_allianceChooser =
      new SendableChooser<CommandSelector>();

  private final SendableChooser<Command> m_autoChooser = new SendableChooser<Command>();

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    // Setup auto chooser
    m_autoChooser.setDefaultOption(
        "Drive Forward", new DriveForwardDistance(m_driveTrain, m_fieldSim, 3));
    m_autoChooser.addOption(
        "One Ball Auto",
        new OneBallAuto(m_driveTrain, m_fieldSim, m_indexer, m_flywheel, m_turret, m_vision));
    m_autoChooser.addOption(
        "Two Ball Auto",
        new TwoBallAuto(
            m_driveTrain, m_fieldSim, m_intake, m_indexer, m_flywheel, m_turret, m_vision));
    m_autoChooser.addOption(
        "Group Three Ball Auto",
        new GroupThreeBallAuto(
            m_driveTrain, m_fieldSim, m_intake, m_indexer, m_flywheel, m_turret, m_vision));
    m_autoChooser.addOption(
        "Individual Three Ball Auto",
        new IndividualThreeBallAuto(
            m_driveTrain, m_fieldSim, m_intake, m_indexer, m_flywheel, m_turret, m_vision));
    m_autoChooser.addOption("Test Path", new TestPath(m_driveTrain, m_fieldSim));

    SmartDashboard.putData("Selected Auto", m_autoChooser);

    initializeSubsystems();

    // Configure the button bindings
    configureButtonBindings();
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {
    for (int i = 0; i < leftButtons.length; i++)
      leftButtons[i] = new JoystickButton(leftJoystick, (i + 1));
    for (int i = 0; i < rightButtons.length; i++)
      rightButtons[i] = new JoystickButton(rightJoystick, (i + 1));
    for (int i = 0; i < xBoxButtons.length; i++)
      xBoxButtons[i] = new JoystickButton(xBoxController, (i + 1));
    for (int i = 0; i < xBoxPOVButtons.length; i++)
      xBoxPOVButtons[i] = new POVButton(xBoxController, (i * 45));

    xBoxLeftTrigger =
        new Button(
            () -> xBoxController.getLeftTriggerAxis() > 0.05); // getTrigger());// getRawAxis(2));
    xBoxRightTrigger = new Button(() -> xBoxController.getRightTriggerAxis() > 0.05);

    int baseRPM = 2850;
    xBoxButtons[0].whileHeld(new SetRpmSetpoint(m_flywheel, m_vision, baseRPM));
    xBoxButtons[1].whileHeld(new SetRpmSetpoint(m_flywheel, m_vision, baseRPM + 50));
    xBoxButtons[2].whileHeld(new SetRpmSetpoint(m_flywheel, m_vision, baseRPM + 100));
    xBoxButtons[3].whileHeld(new SetRpmSetpoint(m_flywheel, m_vision, baseRPM + 150));

    // xBoxButtons[6].whenPressed(new SetClimbState(m_climber, true));
    // xBoxButtons[7].whenPressed(new SetClimbState(m_climber, false));
  }

  public void initializeSubsystems() {
    m_driveTrain.setDefaultCommand(
        new SetArcadeDrive(m_driveTrain, leftJoystick::getY, rightJoystick::getX));
    // m_climber.setDefaultCommand(
    //     new SetClimberOutput(m_climber, () -> xBoxController.getRawAxis(5)));
    m_led.setDefaultCommand(
        new GetSubsystemStates(m_led, m_intake, m_vision, m_flywheel, m_climber));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An ExampleCommand will run in autonomous
    return m_autoChooser.getSelected();
  }

  public void robotPeriodic() {}

  public void disabledInit() {
    m_driveTrain.setDriveTrainNeutralMode(DriveTrainNeutralMode.COAST);
    m_driveTrain.setMotorTankDrive(0, 0);
  }

  public void disabledPeriodic() {}

  public void teleopInit() {
    m_driveTrain.setDriveTrainNeutralMode(DriveTrainNeutralMode.COAST);
  }

  public void teleopPeriodic() {}

  public void autonomousInit() {
    if (RobotBase.isReal()) {
      m_driveTrain.resetEncoderCounts();
      m_driveTrain.resetOdometry(
          m_driveTrain.getRobotPoseMeters(), m_fieldSim.getRobotPose().getRotation());
      m_driveTrain.resetAngle();
    } else {
      m_fieldSim.initSim();
      m_driveTrain.resetEncoderCounts();
      m_driveTrain.resetOdometry(
          m_fieldSim.getRobotPose(), m_fieldSim.getRobotPose().getRotation());
      m_driveTrain.resetAngle();
    }
  }

  public void autonomousPeriodic() {}

  public void simulationInit() {
    m_fieldSim.initSim();
  }

  public void simulationPeriodic() {
    m_fieldSim.simulationPeriodic();
  }
}
