interface ErrorMessageProps {
  message: string;
  className?: string;
}

export function ErrorMessage({ message, className = "" }: ErrorMessageProps) {
  return (
    <div className={`p-4 text-red-800 bg-red-50 rounded-lg ${className}`}>
      <p className="text-sm">{message}</p>
    </div>
  );
}
