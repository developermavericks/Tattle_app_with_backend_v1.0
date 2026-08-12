/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useEffect, useRef } from "react";
import { Logo } from "./Logo";
import { ArrowRight, Sparkles } from "lucide-react";
import { motion } from "motion/react";

interface SplashProps {
  onGetStarted: () => void;
}

export const Splash: React.FC<SplashProps> = ({ onGetStarted }) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const bgMeshRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    // Parallax tracking on mouse move
    const handleMouseMove = (e: MouseEvent) => {
      if (!bgMeshRef.current) return;
      const moveX = (e.clientX - window.innerWidth / 2) * 0.015;
      const moveY = (e.clientY - window.innerHeight / 2) * 0.015;
      bgMeshRef.current.style.transform = `translate(calc(-50% + ${moveX}px), calc(-50% + ${moveY}px)) scale(1.1)`;
    };

    window.addEventListener("mousemove", handleMouseMove);

    // Drifting particles on background canvas
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    let animationFrameId: number;
    let particles: Array<{
      x: number;
      y: number;
      size: number;
      speedY: number;
      opacity: number;
    }> = [];

    const initCanvas = () => {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
      particles = [];
      for (let i = 0; i < 40; i++) {
        particles.push({
          x: Math.random() * canvas.width,
          y: Math.random() * canvas.height,
          size: Math.random() * 2 + 0.5,
          speedY: Math.random() * 0.3 + 0.05,
          opacity: Math.random() * 0.4 + 0.1,
        });
      }
    };

    const drawParticles = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      particles.forEach((p) => {
        ctx.fillStyle = "#7bd6d1";
        ctx.globalAlpha = p.opacity;
        ctx.beginPath();
        ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
        ctx.fill();
        p.y -= p.speedY;
        if (p.y < 0) {
          p.y = canvas.height;
          p.x = Math.random() * canvas.width;
        }
      });
      animationFrameId = requestAnimationFrame(drawParticles);
    };

    window.addEventListener("resize", initCanvas);
    initCanvas();
    drawParticles();

    return () => {
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("resize", initCanvas);
      cancelAnimationFrame(animationFrameId);
    };
  }, []);

  return (
    <div className="relative w-full h-screen overflow-hidden bg-background flex flex-col justify-between items-center px-6 py-12 z-10 select-none">
      {/* Background atmospheric meshes */}
      <div className="absolute inset-0 z-0 overflow-hidden pointer-events-none">
        <div
          ref={bgMeshRef}
          className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[140%] h-[140%] gradient-mesh opacity-80 transition-transform duration-300 ease-out"
        />
        <canvas ref={canvasRef} className="absolute inset-0 w-full h-full opacity-40" />
      </div>

      {/* Top Spacer for layout balance */}
      <div className="h-4 w-full z-10" />

      {/* Center Branding Identity */}
      <div className="flex flex-col items-center text-center z-10">
        <motion.div
          initial={{ opacity: 0, y: -30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: "easeOut" }}
          className="relative mb-6"
        >
          {/* Animated Float Container */}
          <motion.div
            animate={{ y: [0, -10, 0] }}
            transition={{ duration: 4, repeat: Infinity, ease: "easeInOut" }}
          >
            <Logo size={160} />
          </motion.div>
        </motion.div>

        {/* Brand Name Headline */}
        <motion.h1
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: 0.2, duration: 0.6 }}
          className="text-white text-5xl font-extrabold tracking-widest uppercase mb-2 font-sans"
          style={{ letterSpacing: "0.2em" }}
        >
          TATTLE
        </motion.h1>

        {/* Tagline */}
        <motion.p
          initial={{ opacity: 0 }}
          animate={{ opacity: 0.8 }}
          transition={{ delay: 0.4, duration: 0.6 }}
          className="text-text-muted text-lg tracking-normal font-medium max-w-xs font-sans"
        >
          Swipe. Skim. Stay sharp.
        </motion.p>
      </div>

      {/* Call to Action Interactive Area */}
      <div className="w-full max-w-sm flex flex-col items-center gap-6 z-10">
        {/* Status indicator badge */}
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5, duration: 0.5 }}
          className="bg-surface-container/60 backdrop-blur-md px-4 py-2 rounded-full border border-white/5 flex items-center gap-2"
        >
          <span className="relative flex h-2 w-2">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-primary-light opacity-75"></span>
            <span className="relative inline-flex rounded-full h-2 w-2 bg-primary"></span>
          </span>
          <span className="text-xs font-bold font-mono tracking-widest text-on-surface/80">
            LIVE UPDATES ACTIVE
          </span>
        </motion.div>

        {/* GET STARTED main button */}
        <motion.button
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.6, duration: 0.6 }}
          whileTap={{ scale: 0.97 }}
          onClick={onGetStarted}
          className="group w-full h-16 bg-primary hover:bg-primary-light text-black font-bold uppercase tracking-widest rounded-full flex items-center justify-center relative overflow-hidden transition-all duration-300 shadow-2xl shadow-primary/20 hover:shadow-primary/35 cursor-pointer"
        >
          <span className="font-semibold text-sm tracking-widest font-sans">Get Started</span>
          <ArrowRight className="absolute right-6 h-5 w-5 stroke-[2.5px] group-hover:translate-x-1.5 transition-transform duration-300" />
          
          {/* Subtle reflex shine overlay */}
          <div className="absolute inset-0 bg-gradient-to-r from-white/10 via-transparent to-white/5 opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none" />
        </motion.button>

        {/* Footer Version Info */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 0.4 }}
          transition={{ delay: 0.8, duration: 0.6 }}
          className="mt-2 text-[10px] font-bold font-mono tracking-widest text-outline uppercase"
        >
          Version 2.0.4 • GEN-Z NEWSSTREAM
        </motion.div>
      </div>
    </div>
  );
};
