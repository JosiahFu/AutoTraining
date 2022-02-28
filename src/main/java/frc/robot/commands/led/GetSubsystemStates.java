// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package frc.robot.commands.led;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Constants;
import frc.robot.subsystems.*;

/** Sets the LED based on the subsystems' statuses */
public class GetSubsystemStates extends CommandBase {
  @SuppressWarnings({"PMD.UnusedPrivateField", "PMD.SingularField"})
  private final LED m_led;

  private final Vision m_vision;
  private final Intake m_intake;
  private final Climber m_climber;

  /** Sets the LED based on the subsystems' statuses */
  public GetSubsystemStates(LED led, Intake intake, Vision vision, Climber climber) {
    m_led = led;
    m_vision = vision;
    m_climber = climber;
    m_intake = intake;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(led);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    m_led.expressState(LED.robotState.Enabled);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    // the prioritized state to be expressed to the LEDs
    boolean Disabled = DriverStation.isDisabled();
    boolean Enabled = !DriverStation.isDisabled();
    boolean Intaking = m_intake.getIntakeState();
    boolean VisionLock = m_vision.getValidTarget(Constants.Vision.CAMERA_POSITION.GOAL);
    boolean Climbing = m_climber.getElevatorClimbState();

    // set in order of priority to be expressed from the least priority to the
    // highest priority
    if (Disabled) {
      m_led.expressState(LED.robotState.Disabled);
    }
    if (Enabled) {
      m_led.expressState(LED.robotState.Enabled);
    }
    if (Intaking) {
      m_led.expressState(LED.robotState.Intaking);
    }
    if (VisionLock) {
      m_led.expressState(LED.robotState.VisionLock);
    }
    if (Climbing) {
      m_led.expressState(LED.robotState.Climbing);
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {}

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }

  @Override
  public boolean runsWhenDisabled() {
    return true;
  }
}
