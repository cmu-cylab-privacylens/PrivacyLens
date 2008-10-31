package ch.SWITCH.aai.uApprove.components;

import javax.servlet.ServletException;

/**
 * @author poroli
 * 
 * Copyright (c) 2005-2006 SWITCH - The Swiss Education & Research Network
 * 
 * 
 * This class encapsulate every application exception caused by the processing
 * of ARP files.
 * 
 * Adpated to new ArpViewer without modification. C.Witzig, 8.12.2005
 * 
 */
public class UApproveException extends ServletException {

  /**
   * serialVersionUID for serialization
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor Constructs a new UApproveException passing the error message
   * 
   * @param message
   */
  public UApproveException(String message) {
    super(message);
  }

  /**
   * Constructor Constructs a new UApproveException passing the error message and the
   * original exception
   * 
   * @param message
   * @param cause
   */
  public UApproveException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructor Constructs a new UApproveException passing the original exception
   * 
   * @param cause
   */
  public UApproveException(Throwable cause) {
    super(cause);
  }
}
