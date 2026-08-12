/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from "react";

interface LogoProps {
  className?: string;
  size?: number;
}

export const Logo: React.FC<LogoProps> = ({ className = "", size = 120 }) => {
  return (
    <div className={`relative flex items-center justify-center ${className}`} style={{ width: size, height: size }}>
      {/* Glow Backing */}
      <div className="absolute inset-2 bg-gradient-to-tr from-[#f5b3ff] via-[#d176ff] to-[#7582ff] rounded-3xl opacity-25 blur-xl animate-pulse-slow" />
      
      {/* SVG Icon */}
      <svg
        viewBox="0 0 200 200"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        className="relative z-10 w-full h-full drop-shadow-[0_0_15px_rgba(209,118,255,0.3)]"
      >
        <defs>
          {/* Main Pink to Blue Gradient matching the uploaded logo exactly */}
          <linearGradient id="tattle-grad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="#f5b3ff" />
            <stop offset="45%" stopColor="#d176ff" />
            <stop offset="100%" stopColor="#7582ff" />
          </linearGradient>
          {/* Sparkles White to Light Pink Gradient */}
          <linearGradient id="sparkle-grad" x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor="#ffffff" />
            <stop offset="100%" stopColor="#f5b3ff" />
          </linearGradient>
        </defs>

        {/* Outer speech bubble with curved tail and smooth squircle shape */}
        <path
          d="M 80,30 L 120,30 C 142,30 155,42 155,65 L 155,105 C 155,128 142,140 120,140 L 85,140 C 78,140 74,152 70,168 C 68,175 64,175 66,168 C 70,152 60,140 45,140 C 45,132 45,125 45,120 L 45,65 C 45,42 58,30 80,30 Z"
          fill="rgba(255, 255, 255, 0.03)"
          stroke="url(#tattle-grad)"
          strokeWidth="9"
          strokeLinecap="round"
          strokeLinejoin="round"
        />

        {/* Inner stylized 'T' with curved sweep matching the upload */}
        <path
          d="M 70,60 L 130,60 L 130,74 L 107,74 L 107,118 C 107,126 93,126 93,118 L 93,90 C 93,78 80,74 80,74 L 70,74 Z"
          fill="#FFFFFF"
          stroke="url(#tattle-grad)"
          strokeWidth="6"
          strokeLinecap="round"
          strokeLinejoin="round"
        />

        {/* Top-Right Sparkles */}
        {/* Tilted vertical pill */}
        <rect
          x="141"
          y="10"
          width="6"
          height="14"
          rx="3"
          fill="url(#sparkle-grad)"
          transform="rotate(15, 144, 17)"
        />
        {/* 4-point star */}
        <path
          d="M 163,12 Q 163,22 173,22 Q 163,22 163,32 Q 163,22 153,22 Q 163,22 163,12 Z"
          fill="url(#sparkle-grad)"
        />
        {/* Tilted horizontal pill */}
        <rect
          x="166"
          y="31"
          width="14"
          height="6"
          rx="3"
          fill="url(#sparkle-grad)"
          transform="rotate(-15, 173, 34)"
        />
      </svg>
    </div>
  );
};
