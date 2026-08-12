/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from "react";
import { Survey } from "../types";
import { BarChart3, ChevronRight, Check, Award, EyeOff, Film, Clock } from "lucide-react";
import { motion, AnimatePresence } from "motion/react";

interface SurveyCardProps {
  survey: Survey;
  onComplete: () => void;
  onSkip: () => void;
  onWatchAd: () => void;
}

export const SurveyCard: React.FC<SurveyCardProps> = ({
  survey,
  onComplete,
  onSkip,
  onWatchAd
}) => {
  const [started, setStarted] = useState<boolean>(false);
  const [currentQuestionIdx, setCurrentQuestionIdx] = useState<number>(0);
  const [selectedAnswers, setSelectedAnswers] = useState<{ [qId: string]: string }>({});
  const [completed, setCompleted] = useState<boolean>(false);

  const handleStart = () => {
    setStarted(true);
  };

  const handleSelectOption = (option: string) => {
    const qId = survey.questions[currentQuestionIdx].id;
    setSelectedAnswers({ ...selectedAnswers, [qId]: option });

    // Transition delay
    setTimeout(() => {
      if (currentQuestionIdx < survey.questions.length - 1) {
        setCurrentQuestionIdx(currentQuestionIdx + 1);
      } else {
        setCompleted(true);
        setTimeout(() => {
          onComplete();
        }, 1200);
      }
    }, 300);
  };

  const totalQuestions = survey.questions.length;
  const currentQuestion = survey.questions[currentQuestionIdx];

  return (
    <div className="w-full bg-[#1F2833] rounded-2xl border border-primary/20 overflow-hidden shadow-2xl relative p-5 flex flex-col justify-between min-h-[440px]">
      
      {/* Decorative background circle */}
      <div className="absolute -right-16 -top-16 w-36 h-36 bg-primary/5 rounded-full blur-xl pointer-events-none" />

      {/* Top logo identifier */}
      <div className="flex items-center justify-between border-b border-white/5 pb-3">
        <div className="flex items-center gap-2">
          <BarChart3 className="text-primary h-5 w-5 stroke-[2.5px]" />
          <span className="text-[10px] font-mono font-extrabold tracking-widest text-primary uppercase">
            TATTLE CHECKPOINT
          </span>
        </div>
        
        {started && !completed && (
          <span className="text-[10px] font-mono font-bold text-outline">
            Step {currentQuestionIdx + 1} of {totalQuestions}
          </span>
        )}
      </div>

      <AnimatePresence mode="wait">
        
        {/* STATE 1: INTRO LANDING */}
        {!started && !completed && (
          <motion.div
            key="intro"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className="flex-1 flex flex-col justify-center text-center py-6 space-y-6"
          >
            {/* Centered Large Vector Logo / Branding */}
            <div className="relative mx-auto w-28 h-28 flex items-center justify-center bg-primary/10 rounded-full border border-primary/25">
              <BarChart3 className="h-14 w-14 text-primary animate-pulse" />
              <div className="absolute -inset-1 bg-gradient-to-tr from-primary to-secondary rounded-full opacity-10 blur-md pointer-events-none" />
            </div>

            <div className="space-y-2">
              <h3 className="text-xl font-extrabold text-white leading-tight">
                Keep your feed 100% ad-free
              </h3>
              <p className="text-xs text-text-muted max-w-[280px] mx-auto leading-relaxed">
                Provide quick feedback on three opinions to clear your next reading session.
              </p>
            </div>

            {/* Start CTA */}
            <button
              onClick={handleStart}
              className="w-full h-14 bg-primary hover:bg-primary-light text-black font-semibold text-xs tracking-widest uppercase rounded-full flex items-center justify-center gap-2 cursor-pointer shadow-lg shadow-primary/10 transition-transform active:scale-98"
            >
              <span>Start Quick Survey</span>
              <ChevronRight className="h-4 w-4 stroke-[2.5px]" />
            </button>
          </motion.div>
        )}

        {/* STATE 2: LIVE QUESTION FLOW */}
        {started && !completed && (
          <motion.div
            key="question"
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -20 }}
            transition={{ duration: 0.25 }}
            className="flex-1 flex flex-col justify-center space-y-5 py-4"
          >
            {/* Question title */}
            <h4 className="text-sm font-extrabold text-white leading-snug">
              {currentQuestion.question}
            </h4>

            {/* Options rows list */}
            <div className="flex flex-col gap-2.5">
              {currentQuestion.options.map((opt) => {
                const isSelected = selectedAnswers[currentQuestion.id] === opt;
                return (
                  <button
                    key={opt}
                    onClick={() => handleSelectOption(opt)}
                    className={`w-full text-left p-3.5 rounded-xl border transition-all cursor-pointer text-xs font-semibold ${
                      isSelected
                        ? "border-primary bg-primary/10 text-white"
                        : "border-white/5 bg-surface-container-low hover:bg-surface-container text-on-surface"
                    }`}
                  >
                    {opt}
                  </button>
                );
              })}
            </div>
          </motion.div>
        )}

        {/* STATE 3: SURVEY COMPLETED SUCCESS */}
        {completed && (
          <motion.div
            key="completed"
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="flex-1 flex flex-col justify-center text-center py-6 space-y-4"
          >
            <div className="w-16 h-16 bg-secondary/20 rounded-full border border-secondary/35 flex items-center justify-center mx-auto text-secondary animate-bounce">
              <Check className="h-8 w-8 stroke-[3px]" />
            </div>

            <div className="space-y-1">
              <h3 className="text-lg font-extrabold text-white">Checkpoint Cleared!</h3>
              <p className="text-xs text-secondary tracking-widest font-mono font-bold uppercase">
                Ad-Free active • 3 hours
              </p>
            </div>

            <p className="text-[11px] text-text-muted leading-relaxed max-w-[220px] mx-auto">
              Your session is now unlocked and 100% ad-free. Keep stayin' sharp!
            </p>
          </motion.div>
        )}

      </AnimatePresence>

      {/* Fallback Option links shown in introductory landing wireframe */}
      {!started && (
        <div className="flex flex-col items-center gap-2 pt-4 border-t border-white/5">
          <button
            onClick={onSkip}
            className="text-[11px] font-mono font-bold text-outline hover:text-white uppercase tracking-wider py-1 cursor-pointer flex items-center gap-1.5"
          >
            <Clock className="h-3.5 w-3.5" />
            Remind me later
          </button>
          
          <button
            onClick={onWatchAd}
            className="text-[10px] font-mono font-bold text-outline/60 hover:text-white uppercase tracking-widest py-1 cursor-pointer flex items-center gap-1.5"
          >
            <Film className="h-3.5 w-3.5" />
            Watch a standard video ad instead
          </button>
        </div>
      )}
    </div>
  );
};
