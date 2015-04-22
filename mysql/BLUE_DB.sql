-- phpMyAdmin SQL Dump
-- version 3.3.10.4
-- http://www.phpmyadmin.net
--
-- Servidor: blue.uabc.imeev.com
-- Tiempo de generación: 21-04-2015 a las 17:15:20
-- Versión del servidor: 5.1.56
-- Versión de PHP: 5.4.37

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Base de datos: `blue_testing_db`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `chat`
--

CREATE TABLE IF NOT EXISTS `chat` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `isKid` tinyint(4) NOT NULL COMMENT '0 = Parent, 1 = Kid',
  `kid` varchar(10) NOT NULL,
  `parent` varchar(10) NOT NULL,
  `message` text NOT NULL,
  `timeStamp` varchar(15) NOT NULL,
  `isPic` tinyint(1) NOT NULL,
  `seen` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `isKid` (`isKid`,`kid`,`parent`,`timeStamp`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `contacts`
--

CREATE TABLE IF NOT EXISTS `contacts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `kid` varchar(10) NOT NULL,
  `contact_avatar` tinyint(1) NOT NULL DEFAULT '0',
  `contact_name` varchar(50) NOT NULL,
  `contact_number` varchar(15) NOT NULL,
  `contact_skype` varchar(25) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `kid` (`kid`,`contact_name`,`contact_number`),
  KEY `contact_skype` (`contact_skype`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `kids`
--

CREATE TABLE IF NOT EXISTS `kids` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent` varchar(10) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `sexo` tinyint(1) DEFAULT NULL,
  `last_active` varchar(15) NOT NULL,
  `coords` varchar(50) DEFAULT NULL,
  `avatar` tinyint(1) DEFAULT NULL,
  `default_coords` varchar(50) DEFAULT NULL,
  `coord_address` text,
  PRIMARY KEY (`id`),
  KEY `parent` (`parent`,`nombre`,`sexo`,`last_active`),
  KEY `coords` (`coords`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `notifs`
--

CREATE TABLE IF NOT EXISTS `notifs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `kid` varchar(10) NOT NULL,
  `parent` varchar(10) NOT NULL,
  `message` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `kid` (`kid`,`parent`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `parents`
--

CREATE TABLE IF NOT EXISTS `parents` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  `username` varchar(25) DEFAULT NULL,
  `email` varchar(50) NOT NULL,
  `tel` varchar(15) NOT NULL,
  `pass` varchar(50) NOT NULL,
  `passReset` varchar(15) DEFAULT NULL,
  `avatar` tinyint(1) NOT NULL DEFAULT '0',
  `skype` varchar(25) DEFAULT NULL,
  `dist_radius` int(11) NOT NULL DEFAULT '25',
  `exitPin` varchar(4) NOT NULL DEFAULT '1234',
  `action` varchar(10) NOT NULL DEFAULT 'call',
  `last_active` varchar(15) DEFAULT NULL,
  `token` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `nombre` (`nombre`,`email`,`pass`),
  KEY `token` (`token`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8;
