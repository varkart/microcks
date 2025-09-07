/*
 * Copyright The Microcks Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.microcks.util;

import io.github.microcks.domain.ParameterConstraint;
import io.github.microcks.domain.ParameterLocation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Test for ParameterConstraintUtil class.
 * @author laurent
 */
class ParameterConstraintUtilTest {

   @Test
   void testValidateHeaderConstraint() {
      // Setup mock request
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      when(request.getHeader("Authorization")).thenReturn("Bearer token123");

      // Setup constraint
      ParameterConstraint constraint = new ParameterConstraint();
      constraint.setName("Authorization");
      constraint.setIn(ParameterLocation.header);
      constraint.setRequired(true);

      // Test validation - should pass
      String result = ParameterConstraintUtil.validateConstraint(request, constraint);
      assertNull(result);
   }

   @Test
   void testValidateHeaderConstraintMissing() {
      // Setup mock request
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      when(request.getHeader("Authorization")).thenReturn(null);

      // Setup constraint
      ParameterConstraint constraint = new ParameterConstraint();
      constraint.setName("Authorization");
      constraint.setIn(ParameterLocation.header);
      constraint.setRequired(true);

      // Test validation - should fail
      String result = ParameterConstraintUtil.validateConstraint(request, constraint);
      assertEquals("Parameter Authorization is required", result);
   }

   @Test
   void testValidateQueryConstraint() {
      // Setup mock request
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      when(request.getParameter("page")).thenReturn("1");

      // Setup constraint
      ParameterConstraint constraint = new ParameterConstraint();
      constraint.setName("page");
      constraint.setIn(ParameterLocation.query);
      constraint.setRequired(true);
      constraint.setMustMatchRegexp("\\d+");

      // Test validation - should pass
      String result = ParameterConstraintUtil.validateConstraint(request, constraint);
      assertNull(result);
   }

   @Test
   void testValidateQueryConstraintRegexpFail() {
      // Setup mock request
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      when(request.getParameter("page")).thenReturn("abc");

      // Setup constraint
      ParameterConstraint constraint = new ParameterConstraint();
      constraint.setName("page");
      constraint.setIn(ParameterLocation.query);
      constraint.setRequired(true);
      constraint.setMustMatchRegexp("\\d+");

      // Test validation - should fail
      String result = ParameterConstraintUtil.validateConstraint(request, constraint);
      assertEquals("Parameter page should match \\d+", result);
   }

   @Test
   void testValidateCookieConstraint() {
      // Setup mock request with cookies
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      Cookie sessionCookie = new Cookie("sessionId", "abc123");
      Cookie[] cookies = { sessionCookie };
      when(request.getCookies()).thenReturn(cookies);

      // Setup constraint
      ParameterConstraint constraint = new ParameterConstraint();
      constraint.setName("sessionId");
      constraint.setIn(ParameterLocation.cookie);
      constraint.setRequired(true);

      // Test validation - should pass
      String result = ParameterConstraintUtil.validateConstraint(request, constraint);
      assertNull(result);
   }

   @Test
   void testValidateCookieConstraintMissing() {
      // Setup mock request with no cookies
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      when(request.getCookies()).thenReturn(null);

      // Setup constraint
      ParameterConstraint constraint = new ParameterConstraint();
      constraint.setName("sessionId");
      constraint.setIn(ParameterLocation.cookie);
      constraint.setRequired(true);

      // Test validation - should fail
      String result = ParameterConstraintUtil.validateConstraint(request, constraint);
      assertEquals("Parameter sessionId is required", result);
   }

   @Test
   void testValidateCookieConstraintNotFound() {
      // Setup mock request with different cookies
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      Cookie differentCookie = new Cookie("otherId", "xyz789");
      Cookie[] cookies = { differentCookie };
      when(request.getCookies()).thenReturn(cookies);

      // Setup constraint for a cookie that doesn't exist
      ParameterConstraint constraint = new ParameterConstraint();
      constraint.setName("sessionId");
      constraint.setIn(ParameterLocation.cookie);
      constraint.setRequired(true);

      // Test validation - should fail
      String result = ParameterConstraintUtil.validateConstraint(request, constraint);
      assertEquals("Parameter sessionId is required", result);
   }

   @Test
   void testValidateCookieConstraintRegexp() {
      // Setup mock request with cookies
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      Cookie sessionCookie = new Cookie("sessionId", "session_abc123");
      Cookie[] cookies = { sessionCookie };
      when(request.getCookies()).thenReturn(cookies);

      // Setup constraint with regexp
      ParameterConstraint constraint = new ParameterConstraint();
      constraint.setName("sessionId");
      constraint.setIn(ParameterLocation.cookie);
      constraint.setRequired(true);
      constraint.setMustMatchRegexp("session_[a-z0-9]+");

      // Test validation - should pass
      String result = ParameterConstraintUtil.validateConstraint(request, constraint);
      assertNull(result);
   }

   @Test
   void testValidateCookieConstraintRegexpFail() {
      // Setup mock request with cookies
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      Cookie sessionCookie = new Cookie("sessionId", "invalid_session");
      Cookie[] cookies = { sessionCookie };
      when(request.getCookies()).thenReturn(cookies);

      // Setup constraint with regexp that won't match
      ParameterConstraint constraint = new ParameterConstraint();
      constraint.setName("sessionId");
      constraint.setIn(ParameterLocation.cookie);
      constraint.setRequired(true);
      constraint.setMustMatchRegexp("session_[a-z0-9]+");

      // Test validation - should fail
      String result = ParameterConstraintUtil.validateConstraint(request, constraint);
      assertEquals("Parameter sessionId should match session_[a-z0-9]+", result);
   }

   @Test
   void testValidateOptionalCookieConstraintMissing() {
      // Setup mock request with no cookies
      HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
      when(request.getCookies()).thenReturn(null);

      // Setup optional constraint
      ParameterConstraint constraint = new ParameterConstraint();
      constraint.setName("optionalCookie");
      constraint.setIn(ParameterLocation.cookie);
      constraint.setRequired(false);

      // Test validation - should pass (optional parameter can be missing)
      String result = ParameterConstraintUtil.validateConstraint(request, constraint);
      assertNull(result);
   }
}
